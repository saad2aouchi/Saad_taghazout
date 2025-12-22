import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'api_config.dart';
import '../models/auth_response.dart';

/// User role enum matching backend roles.
enum UserRole { client, host }

/// Service class for authentication API calls.
/// 
/// Handles login, registration, and token storage.
class AuthService {
  static const _storage = FlutterSecureStorage();
  static const _accessTokenKey = 'access_token';
  static const _refreshTokenKey = 'refresh_token';
  static const _userEmailKey = 'user_email';
  static const _userIdKey = 'user_id';
  static const _userRoleKey = 'user_role';

  /// Login with email and password.
  /// 
  /// Returns [AuthResponse] on success, throws [AuthException] on failure.
  static Future<AuthResponse> login(String email, String password) async {
    final response = await http.post(
      Uri.parse(ApiConfig.loginUrl),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'password': password,
      }),
    );

    return _handleAuthResponse(response);
  }

  /// Register a new client user.
  /// 
  /// Returns [AuthResponse] on success, throws [AuthException] on failure.
  static Future<AuthResponse> registerClient({
    required String email,
    required String password,
    String? firstName,
    String? lastName,
  }) async {
    final response = await http.post(
      Uri.parse(ApiConfig.registerClientUrl),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'password': password,
        if (firstName != null) 'firstName': firstName,
        if (lastName != null) 'lastName': lastName,
      }),
    );

    return _handleAuthResponse(response);
  }

  /// Register a new host user.
  /// 
  /// Returns [AuthResponse] on success, throws [AuthException] on failure.
  static Future<AuthResponse> registerHost({
    required String email,
    required String password,
    String? firstName,
    String? lastName,
    String? organizationName,
  }) async {
    final response = await http.post(
      Uri.parse(ApiConfig.registerHostUrl),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'password': password,
        if (firstName != null) 'firstName': firstName,
        if (lastName != null) 'lastName': lastName,
        if (organizationName != null) 'organizationName': organizationName,
      }),
    );

    return _handleAuthResponse(response);
  }

  /// Handle auth response and store tokens on success.
  static Future<AuthResponse> _handleAuthResponse(http.Response response) async {
    if (response.statusCode == 200 || response.statusCode == 201) {
      final authResponse = AuthResponse.fromJson(jsonDecode(response.body));
      await _storeTokens(authResponse);
      return authResponse;
    } else if (response.statusCode == 401) {
      throw AuthException('Invalid email or password');
    } else if (response.statusCode == 409) {
      throw AuthException('Email already exists');
    } else if (response.statusCode == 400) {
      // Parse validation errors from response
      try {
        final body = jsonDecode(response.body);
        final message = body['message'] ?? 'Validation error';
        throw AuthException(message);
      } catch (e) {
        if (e is AuthException) rethrow;
        throw AuthException('Invalid request');
      }
    } else {
      throw AuthException('Server error. Please try again later.');
    }
  }

  /// Store tokens securely.
  static Future<void> _storeTokens(AuthResponse response) async {
    await _storage.write(key: _accessTokenKey, value: response.accessToken);
    await _storage.write(key: _refreshTokenKey, value: response.refreshToken);
    await _storage.write(key: _userEmailKey, value: response.email);
    if (response.userId != null) {
      await _storage.write(key: _userIdKey, value: response.userId.toString());
    }
    if (response.role != null) {
      await _storage.write(key: _userRoleKey, value: response.role);
    }
  }

  /// Get stored access token.
  static Future<String?> getAccessToken() async {
    return await _storage.read(key: _accessTokenKey);
  }

  /// Get stored user email.
  static Future<String?> getUserEmail() async {
    return await _storage.read(key: _userEmailKey);
  }

  /// Get stored user ID.
  static Future<String?> getUserId() async {
    return await _storage.read(key: _userIdKey);
  }

  /// Get stored user role.
  static Future<String?> getUserRole() async {
    return await _storage.read(key: _userRoleKey);
  }

  /// Clear stored tokens (logout).
  static Future<void> logout() async {
    await _storage.deleteAll();
  }

  /// Check if user is logged in.
  static Future<bool> isLoggedIn() async {
    final token = await getAccessToken();
    return token != null && token.isNotEmpty;
  }
}

/// Exception for authentication errors.
class AuthException implements Exception {
  final String message;
  AuthException(this.message);

  @override
  String toString() => message;
}
