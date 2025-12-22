import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'login_screen.dart';
import 'add_hostel_screen.dart';
import '../services/listing_service.dart';
import '../services/auth_service.dart';
import '../models/listing.dart';

class HostHomeScreen extends StatefulWidget {
  const HostHomeScreen({super.key});

  @override
  State<HostHomeScreen> createState() => _HostHomeScreenState();
}

class _HostHomeScreenState extends State<HostHomeScreen> {
  final _listingService = ListingService();
  late Future<List<Listing>> _listingsFuture;

  @override
  void initState() {
    super.initState();
    _refreshListings();
  }

  void _refreshListings() {
    setState(() {
      _listingsFuture = _fetchListings();
    });
  }

  Future<List<Listing>> _fetchListings() async {
    final userId = await AuthService.getUserId();
    return _listingService.getListings(hostId: userId);
  }

  Future<void> _logout(BuildContext context) async {
    const storage = FlutterSecureStorage();
    await storage.deleteAll();
    if (context.mounted) {
       Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (context) => const LoginScreen()),
        (route) => false,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFDF5E6),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          "HOST DASHBOARD",
          style: TextStyle(color: Color(0xFFD35400), fontWeight: FontWeight.bold, letterSpacing: 1),
        ),
        centerTitle: true,
        actions: [
          IconButton(
            onPressed: () async {
              final result = await Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const AddHostelScreen()),
              );
              if (result == true) {
                _refreshListings();
              }
            },
            icon: const Icon(Icons.add, color: Color(0xFFD35400), size: 30),
            tooltip: 'Add Property',
          ),
          IconButton(
            onPressed: () => _refreshListings(),
            icon: const Icon(Icons.refresh, color: Color(0xFFD35400)),
          ),
          IconButton(
            onPressed: () => _logout(context),
            icon: const Icon(Icons.logout, color: Color(0xFFD35400)),
          ),
        ],
      ),
      body: Center(
        child: Container(
          constraints: const BoxConstraints(maxWidth: 800),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                "Your Listings",
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.black87),
              ),
              const SizedBox(height: 10),
              Expanded(
                child: FutureBuilder<List<Listing>>(
                  future: _listingsFuture,
                  builder: (context, snapshot) {
                    if (snapshot.connectionState == ConnectionState.waiting) {
                      return const Center(child: CircularProgressIndicator(color: Color(0xFFE67E22)));
                    } else if (snapshot.hasError) {
                      return Center(child: SelectableText('Error: ${snapshot.error}')); // Selectable for copy
                    } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                      return _buildEmptyState();
                    }

                    final listings = snapshot.data!;
                    return ListView.builder(
                      itemCount: listings.length,
                      itemBuilder: (context, index) {
                        return _buildListingCard(listings[index]);
                      },
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.house_siding_rounded, size: 80, color: Colors.grey[400]),
          const SizedBox(height: 20),
          Text(
            "No properties listed by you.",
            style: TextStyle(fontSize: 18, color: Colors.grey[600]),
          ),
          const SizedBox(height: 10),
          TextButton(
            onPressed: () async {
               final result = await Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const AddHostelScreen()),
              );
              if (result == true) _refreshListings();
            },
            child: const Text("Add your first hostel", style: TextStyle(fontSize: 16)),
          ),
        ],
      ),
    );
  }

  Widget _buildListingCard(Listing listing) {
    final details = listing.hostelDetails;
    if (details == null) return const SizedBox.shrink();

    return Container(
      margin: const EdgeInsets.only(bottom: 20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Image
          ClipRRect(
            borderRadius: const BorderRadius.vertical(top: Radius.circular(16)),
            child: Image.network(
              details.images.isNotEmpty ? details.images.first : 'https://placehold.co/600x400',
              height: 180,
              width: double.infinity,
              fit: BoxFit.cover,
              errorBuilder: (context, error, stackTrace) => Container(
                height: 180, 
                color: Colors.grey[300], 
                child: const Icon(Icons.broken_image, size: 50, color: Colors.grey)
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      details.name,
                      style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: const Color(0xFFFDF5E6),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        "€${details.pricePerNight.amount}/night",
                        style: const TextStyle(color: Color(0xFFD35400), fontWeight: FontWeight.bold),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    const Icon(Icons.location_on, size: 16, color: Colors.grey),
                    const SizedBox(width: 4),
                    Text(
                      "${details.address.city}, ${details.address.street}",
                      style: TextStyle(color: Colors.grey[600]),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  details.description,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(color: Colors.grey[700]),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
