import 'dart:convert';

enum ListingType { HOSTEL, SURF_CAMP, ACTIVITY, RENTAL }
enum Amenity { WIFI, POOL, BREAKFAST, AC, KITCHEN, SHUTTLE }

class Listing {
  final String? id; // Backend UUID is String
  final int? hostId;
  final ListingType type;
  final HostelDetails? hostelDetails;

  Listing({
    this.id,
    this.hostId,
    required this.type,
    this.hostelDetails,
  });

  factory Listing.fromJson(Map<String, dynamic> json) {
    return Listing(
      id: json['id'],
      hostId: json['hostId'],
      type: ListingType.values.firstWhere((e) => e.toString().split('.').last == json['type']),
      hostelDetails: json['hostelDetails'] != null ? HostelDetails.fromJson(json['hostelDetails']) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (hostId != null) 'hostId': hostId,
      'type': type.toString().split('.').last,
      'hostelDetails': hostelDetails?.toJson(),
    };
  }
}

class HostelDetails {
  final String name;
  final String description;
  final Address address;
  final Money pricePerNight;
  final Rating rating;
  final Set<Amenity> amenities;
  final Availability availability;
  final List<String> images;

  HostelDetails({
    required this.name,
    required this.description,
    required this.address,
    required this.pricePerNight,
    required this.rating,
    required this.amenities,
    required this.availability,
    required this.images,
  });

  factory HostelDetails.fromJson(Map<String, dynamic> json) {
    return HostelDetails(
      name: json['name'],
      description: json['description'],
      address: Address.fromJson(json['address']),
      pricePerNight: Money.fromJson(json['pricePerNight']),
      rating: Rating.fromJson(json['rating']),
      amenities: (json['amenities'] as List).map((e) => Amenity.values.firstWhere((a) => a.toString().split('.').last == e)).toSet(),
      availability: Availability.fromJson(json['availability']),
      images: List<String>.from(json['images'].map((x) => x is String ? x : x['url'])), // Handle ImageUrl object or string
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'description': description,
      'address': address.toJson(),
      'pricePerNight': pricePerNight.toJson(),
      'rating': rating.toJson(),
      'amenities': amenities.map((e) => e.toString().split('.').last).toList(),
      'availability': availability.toJson(),
      'images': images.map((url) => {'url': url}).toList(), // Backend expects List<ImageUrl> objects
    };
  }
}

class Address {
  final String city;
  final String country;
  final String street;

  Address({required this.city, required this.country, required this.street});

  factory Address.fromJson(Map<String, dynamic> json) {
    return Address(city: json['city'], country: json['country'], street: json['street']);
  }

  Map<String, dynamic> toJson() => {'city': city, 'country': country, 'street': street};
}

class Money {
  final double amount; // Using double for simplicity in Dart
  final String currency;

  Money({required this.amount, required this.currency});

  factory Money.fromJson(Map<String, dynamic> json) {
    return Money(amount: (json['amount'] as num).toDouble(), currency: json['currency']);
  }

  Map<String, dynamic> toJson() => {'amount': amount, 'currency': currency};
}

class Rating {
  final double score;
  final int reviewCount;

  Rating({required this.score, required this.reviewCount});

  factory Rating.fromJson(Map<String, dynamic> json) {
    return Rating(score: (json['score'] as num).toDouble(), reviewCount: json['reviewCount']);
  }

  Map<String, dynamic> toJson() => {'score': score, 'reviewCount': reviewCount};
}

class Availability {
  final int totalBeds;
  final int availableBeds;

  Availability({required this.totalBeds, required this.availableBeds});

  factory Availability.fromJson(Map<String, dynamic> json) {
    return Availability(totalBeds: json['totalBeds'], availableBeds: json['availableBeds']);
  }

  Map<String, dynamic> toJson() => {'totalBeds': totalBeds, 'availableBeds': availableBeds};
}
