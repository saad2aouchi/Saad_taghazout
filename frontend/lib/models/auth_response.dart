/// Model class matching the backend AuthResponse DTO.
/// 
/// Returned after successful login or registration.
class AuthResponse {
  final String email;
  final String? firstName;
  final String? lastName;
  final String accessToken;
  final String refreshToken;

  AuthResponse({
    required this.email,
    this.firstName,
    this.lastName,
    required this.accessToken,
    required this.refreshToken,
  });

  /// Factory constructor to create AuthResponse from JSON.
  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      email: json['email'] as String,
      firstName: json['firstName'] as String?,
      lastName: json['lastName'] as String?,
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
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
    };
  }
}
