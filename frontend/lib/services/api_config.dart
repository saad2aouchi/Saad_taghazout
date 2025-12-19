/// API Configuration for the Taghazout app.
/// 
/// Contains base URL and API versioning constants.
class ApiConfig {
  /// Base URL for the auth-service (standalone mode).
  /// For production, use the API gateway on port 8080.
  /// For standalone testing without Docker, use auth-service directly on port 8090.
  static const String baseUrl = 'http://localhost:8090';
  
  /// API version prefix.
  static const String apiVersion = '/api/v1';
  
  /// Auth endpoints.
  static const String authEndpoint = '$apiVersion/auth';
  static const String loginEndpoint = '$authEndpoint/login';
  static const String registerClientEndpoint = '$authEndpoint/register/client';
  static const String registerHostEndpoint = '$authEndpoint/register/host';
  
  /// Full URLs.
  static String get loginUrl => '$baseUrl$loginEndpoint';
  static String get registerClientUrl => '$baseUrl$registerClientEndpoint';
  static String get registerHostUrl => '$baseUrl$registerHostEndpoint';
}
