import 'package:flutter/material.dart';
import 'login_screen.dart';

void main() {
  runApp(const TaghazoutApp());
}

class TaghazoutApp extends StatelessWidget {
  const TaghazoutApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Taghazout Hostels',
      theme: ThemeData(
        // The warm sunset palette
        scaffoldBackgroundColor: const Color(0xFFFDF5E6), 
        primaryColor: const Color(0xFFE67E22),
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFFE67E22)),
        useMaterial3: true,
      ),
      home: const LoginScreen(),
    );
  }
}
