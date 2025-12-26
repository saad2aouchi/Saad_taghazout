package com.taghazout.authservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for Auth Service
 * 
 * SOLID Principles Applied:
 * - SRP: Single responsibility - only configures security
 * - OCP: Open for extension via custom filters/handlers
 * - DIP: Depends on Spring Security abstractions (HttpSecurity,
 * SecurityFilterChain)
 * 
 * Design Decisions:
 * - Stateless session management (JWT-based authentication)
 * - BCrypt password encoder with strength 12 (balance between security and
 * performance)
 * - Public access to registration and login endpoints
 * - H2 console enabled in development profile only
 * - Frame options configured for H2 console compatibility
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/register/client",
            "/api/v1/auth/register/host",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
    };

    private static final String[] ACTUATOR_ENDPOINTS = {
            "/actuator/health",
            "/actuator/info"
    };

    private static final String[] OPENAPI_ENDPOINTS = {
            "/auth/openapi.json",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private final Environment environment;

    public SecurityConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * Configures HTTP security with stateless session management.
     * 
     * Security Rules:
     * 1. CSRF disabled - not needed for stateless JWT authentication
     * 2. Stateless sessions - no server-side session storage
     * 3. Public endpoints - registration, login, actuator health
     * 4. H2 console - allowed only in development profile
     * 5. Frame options - same origin for H2 console
     * 
     * @param http HttpSecurity builder
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // === Disable CSRF ===
                // Safe for stateless JWT authentication (no cookies)
                .csrf(AbstractHttpConfigurer::disable)

                // === CORS Configuration ===
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // === Session Management ===
                // Stateless - no HttpSession created or used
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // === Authorization Rules ===
                .authorizeHttpRequests(auth -> {
                    // Public authentication endpoints
                    auth.requestMatchers(PUBLIC_ENDPOINTS).permitAll();

                    // Actuator endpoints (health check, info)
                    auth.requestMatchers(ACTUATOR_ENDPOINTS).permitAll();

                    // OpenAPI documentation endpoints
                    auth.requestMatchers(OPENAPI_ENDPOINTS).permitAll();

                    // H2 Console - only in development
                    if (isDevelopmentProfile()) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    // All other endpoints require authentication
                    auth.anyRequest().authenticated();
                })

                // === Frame Options ===
                // Allow frames from same origin (required for H2 console)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * Password encoder bean using BCrypt algorithm.
     * 
     * BCrypt Configuration:
     * - Strength: 12 rounds (2^12 = 4,096 iterations)
     * - Security: Strong enough for production (OWASP recommended minimum is 10)
     * - Performance: ~200-500ms per hash (acceptable for login operations)
     * 
     * Why BCrypt?
     * - Adaptive: Can increase rounds as hardware improves
     * - Salt built-in: Automatic random salt per password
     * - Slow by design: Resistant to brute-force attacks
     * - Industry standard: OWASP, NIST recommended
     * 
     * @return BCryptPasswordEncoder with strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * CORS configuration source for local development and production.
     * Allows Flutter web app origins.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow all origins for development/standalone
        if (isDevelopmentProfile()) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            // Add specific origins for production
            configuration.setAllowedOrigins(List.of("http://localhost:52903", "http://localhost:8080"));
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token", "RefreshToken"));
        configuration.setExposedHeaders(List.of("x-auth-token"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Checks if application is running in development profile.
     * 
     * @return true if 'dev', 'development', 'docker', or 'standalone' profile is
     *         active
     */
    private boolean isDevelopmentProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains("dev")
                || Arrays.asList(activeProfiles).contains("development")
                || Arrays.asList(activeProfiles).contains("docker")
                || Arrays.asList(activeProfiles).contains("standalone");
    }
}
