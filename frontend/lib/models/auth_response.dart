/// Model class matching the backend AuthResponse DTO.
/// 
/// Returned after successful login or registration.
class AuthResponse {
  final String email;
  final String? firstName;
  final String? lastName;
  final String accessToken;
  final String? refreshToken;
  final String role; // New field
  final int? userId;

  AuthResponse({
    required this.email,
    this.firstName,
    this.lastName,
    required this.accessToken,
    this.refreshToken,
    required this.role,
    this.userId,
  });

  /// Factory constructor to create AuthResponse from JSON.
  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      email: json['email'] as String,
      firstName: json['firstName'] as String?,
      lastName: json['lastName'] as String?,
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String?,
      role: json['role'] as String? ?? 'CLIENT', // Default to CLIENT if missing (backward compatibility)
      userId: json['userId'] as int?,
    );
  }

  /// Convert to JSON map.
  Map<String, dynamic> toJson() {
    return {
      'email': email,
      'firstName': firstName,
      'lastName': lastName,
      'accessToken': accessToken,
      'refreshToken': refreshToken,
      'role': role,
    };
  }
}
