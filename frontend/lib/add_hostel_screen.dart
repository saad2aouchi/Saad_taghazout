import 'package:flutter/material.dart';
import '../models/listing.dart';
import '../services/listing_service.dart';
import '../services/auth_service.dart';

class AddHostelScreen extends StatefulWidget {
  const AddHostelScreen({super.key});

  @override
  State<AddHostelScreen> createState() => _AddHostelScreenState();
}

class _AddHostelScreenState extends State<AddHostelScreen> {
  final _formKey = GlobalKey<FormState>();
  final _listingService = ListingService();
  bool _isLoading = false;

  // Controllers
  final _nameController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _cityController = TextEditingController();
  final _streetController = TextEditingController();
  final _priceController = TextEditingController();
  final _imageUrlController = TextEditingController();

  final Set<Amenity> _selectedAmenities = {};

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    _cityController.dispose();
    _streetController.dispose();
    _priceController.dispose();
    _imageUrlController.dispose();
    super.dispose();
  }

  Future<void> _submitLog() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      final userIdStr = await AuthService.getUserId();
      final userId = userIdStr != null ? int.tryParse(userIdStr) : null;

      final listing = Listing(
        hostId: userId,
        type: ListingType.HOSTEL,
        hostelDetails: HostelDetails(
          name: _nameController.text,
          description: _descriptionController.text,
          address: Address(
            city: _cityController.text,
            country: 'Morocco', // Default for Taghazout App
            street: _streetController.text,
          ),
          pricePerNight: Money(
            amount: double.parse(_priceController.text),
            currency: 'EUR', // Default
          ),
          rating: Rating(score: 0, reviewCount: 0),
          amenities: _selectedAmenities,
          availability: Availability(totalBeds: 10, availableBeds: 10), // Defaults
          images: [_imageUrlController.text.isNotEmpty ? _imageUrlController.text : 'https://placehold.co/600x400'],
        ),
      );

      await _listingService.createListing(listing);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Hostel added successfully!'), backgroundColor: Colors.green),
        );
        Navigator.pop(context, true); // Return true to refresh
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: ${e.toString()}'), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFDF5E6),
      appBar: AppBar(
        title: const Text("List Your Hostel", style: TextStyle(color: Color(0xFFD35400), fontWeight: FontWeight.bold)),
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: const IconThemeData(color: Color(0xFFD35400)),
      ),
      body: Center(
        child: Container(
          constraints: const BoxConstraints(maxWidth: 600),
          padding: const EdgeInsets.all(20),
          child: Form(
            key: _formKey,
            child: ListView(
              children: [
                _buildTextField(_nameController, "Hostel Name", Icons.hotel),
                const SizedBox(height: 15),
                _buildTextField(_descriptionController, "Description", Icons.description, maxLines: 3),
                const SizedBox(height: 15),
                Row(
                  children: [
                    Expanded(child: _buildTextField(_cityController, "City", Icons.location_city)),
                    const SizedBox(width: 15),
                    Expanded(child: _buildTextField(_streetController, "Street", Icons.map)),
                  ],
                ),
                const SizedBox(height: 15),
                _buildTextField(_priceController, "Price per Night (EUR)", Icons.euro, isNumber: true),
                const SizedBox(height: 15),
                _buildTextField(_imageUrlController, "Image URL", Icons.image),
                const SizedBox(height: 20),
                
                const Text("Amenities", style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                Wrap(
                  spacing: 8,
                  children: Amenity.values.map((amenity) {
                    final isSelected = _selectedAmenities.contains(amenity);
                    return FilterChip(
                      label: Text(amenity.toString().split('.').last),
                      selected: isSelected,
                      selectedColor: const Color(0xFFE67E22).withOpacity(0.2),
                      checkmarkColor: const Color(0xFFD35400),
                      onSelected: (bool selected) {
                        setState(() {
                          if (selected) {
                            _selectedAmenities.add(amenity);
                          } else {
                            _selectedAmenities.remove(amenity);
                          }
                        });
                      },
                    );
                  }).toList(),
                ),
                const SizedBox(height: 30),

                SizedBox(
                  height: 50,
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFFE67E22),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                    onPressed: _isLoading ? null : _submitLog,
                    child: _isLoading 
                      ? const CircularProgressIndicator(color: Colors.white) 
                      : const Text("PUBLISH LISTING", style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTextField(TextEditingController controller, String label, IconData icon, {bool isNumber = false, int maxLines = 1}) {
    return TextFormField(
      controller: controller,
      keyboardType: isNumber ? TextInputType.number : TextInputType.text,
      maxLines: maxLines,
      decoration: InputDecoration(
        labelText: label,
        prefixIcon: Icon(icon, color: const Color(0xFFD35400)),
        filled: true,
        fillColor: Colors.white,
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
      ),
      validator: (value) => value == null || value.isEmpty ? "$label is required" : null,
    );
  }
}
