/// API Configuration for the Taghazout app.
/// 
/// Contains base URL and API versioning constants.
class ApiConfig {
  /// Base URL for the auth-service (standalone mode).
  /// For production, use the API gateway on port 8080.
  /// For standalone testing without Docker, use auth-service directly on port 8090.
  
  /// API version prefix.
  static const String baseUrl = 'http://localhost:8090/api/v1'; // Auth Service
  static const String listingBaseUrl = 'http://127.0.0.1:8082/api/v1'; // Listing Service (Use 127.0.0.1 for CORS safety)

  // Auth Endpoints
  static const String login = '/auth/login';
  static const String registerClient = '/auth/register/client';
  static const String registerHost = '/auth/register/host';
  
  // Listing Endpoints
  static const String listings = '/listings';
  
  /// Full URLs.
  static String get loginUrl => '$baseUrl$login';
  static String get registerClientUrl => '$baseUrl$registerClient';
  static String get registerHostUrl => '$baseUrl$registerHost';
  static String get listingsUrl => '$listingBaseUrl$listings';
}
