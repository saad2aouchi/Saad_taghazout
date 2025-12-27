import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../services/api_config.dart';
import '../models/listing.dart';

class ListingService {
  final _storage = const FlutterSecureStorage();

  Future<Listing> createListing(Listing listing) async {
    final token = await _storage.read(key: 'access_token');
    
    // Note: If Listing Service is unsecured (no JWT check), token might be ignored by backend,
    // but sending it is good practice if security is enabled later.
    final response = await http.post(
      Uri.parse(ApiConfig.listingsUrl),
      headers: {
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      },
      body: jsonEncode(listing.toJson()),
    );

    if (response.statusCode == 201) {
      return Listing.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to create listing: ${response.body}');
    }
  }

  Future<List<Listing>> getListings({String? hostId}) async {
    final token = await _storage.read(key: 'access_token');
    final uri = Uri.parse(ApiConfig.listingsUrl).replace(
        queryParameters: hostId != null ? {'hostId': hostId} : null);

    final response = await http.get(
      uri,
      headers: {
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> body = jsonDecode(response.body);
      return body.map((dynamic item) => Listing.fromJson(item)).toList();
    } else {
      throw Exception('Failed to load listings');
    }
  }
}
