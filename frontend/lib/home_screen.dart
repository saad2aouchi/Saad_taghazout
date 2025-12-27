/* import 'package:flutter/material.dart';
import 'home_screen_client.dart';
import 'home_screen_host.dart';
import 'services/auth_service.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  String? _role;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadRole();
  }

  Future<void> _loadRole() async {
    final role = await AuthService.getUserRole();
    if (mounted) {
      setState(() {
        _role = role;
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(
        backgroundColor: Color(0xFFFDF5E6),
        body: Center(
          child: CircularProgressIndicator(color: Color(0xFFE67E22)),
        ),
      );
    }

    // Role enum in AuthService is 'UserRole.host', but DB might store 'HOST' or 'host' or 'UserRole.host'.
    // AuthService._storeTokens saves 'response.role'.
    // Steps 109 shows AuthService saves response.role.
    // I need to check what String the backend returns for role.
    // It's likely "HOST" or "CLIENT" (enum names usually uppercase or matching).
    // The previous code checked for 'HOST'. I'll stick to case-insensitive check to be safe.
    
    // Robust check for host role (handles "HOST", "host", "UserRole.host")
    if (_role != null && _role!.toString().toUpperCase().contains('HOST')) {
      return HostHomeScreen(role: _role);
    }
    
    // Default to Client screen for CLIENT or unknown roles
    return ClientHomeScreen(role: _role);
  }
} */