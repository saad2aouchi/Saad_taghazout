import 'package:flutter/material.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // List of hostel sections
    final List<Map<String, dynamic>> categories = [
      {"title": "Ocean View Dorms", "icon": Icons.waves, "count": "12 beds left"},
      {"title": "Private Terrace Rooms", "icon": Icons.wb_sunny_outlined, "count": "2 rooms left"},
      {"title": "Surf Lessons", "icon": Icons.surfing, "count": "Daily at 9:00 AM"},
      {"title": "Yoga Sessions", "icon": Icons.self_improvement, "count": "Sunset class"},
      {"title": "Community Kitchen", "icon": Icons.restaurant, "count": "Open 24/7"},
      {"title": "Airport Shuttle", "icon": Icons.airport_shuttle, "count": "Available 24h"},
    ];

    return Scaffold(
      backgroundColor: const Color(0xFFFDF5E6), // Creamy White background
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          "TAGHAZOUT HOSTELS",
          style: TextStyle(
            color: Color(0xFFD35400), 
            fontWeight: FontWeight.bold, 
            letterSpacing: 2,
            fontSize: 18,
          ),
        ),
        centerTitle: true,
        actions: [
          IconButton(
            onPressed: () => Navigator.pop(context), 
            icon: const Icon(Icons.logout, color: Color(0xFFD35400))
          ),
        ],
      ),
      body: Center(
        // This ConstrainedBox is what makes it work for both PC and Phone
        child: Container(
          constraints: const BoxConstraints(maxWidth: 600), // Limits width on PC
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 25.0, vertical: 20),
                child: Text(
                  "Dashboard",
                  style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: Colors.black87),
                ),
              ),
              Expanded(
                child: ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  itemCount: categories.length,
                  itemBuilder: (context, index) {
                    return Container(
                      margin: const EdgeInsets.only(bottom: 16),
                      padding: const EdgeInsets.all(20),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(24), // Ultra-round edges
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.04),
                            blurRadius: 15,
                            offset: const Offset(0, 5),
                          ),
                        ],
                      ),
                      child: Row(
                        children: [
                          // Icon Circle
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: const Color(0xFFFDF5E6),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(categories[index]['icon'], color: const Color(0xFFE67E22)),
                          ),
                          const SizedBox(width: 20),
                          // Text Section
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  categories[index]['title'],
                                  style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  categories[index]['count'],
                                  style: TextStyle(color: Colors.grey[600], fontSize: 14),
                                ),
                              ],
                            ),
                          ),
                          const Icon(Icons.chevron_right, color: Colors.grey),
                        ],
                      ),
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
}