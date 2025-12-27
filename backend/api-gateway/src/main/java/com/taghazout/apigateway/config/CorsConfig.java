package com.taghazout.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for API Gateway.
 * 
 * This is the SINGLE source of truth for CORS in the gateway.
 * Configurable via 'app.cors.allowed-origins' property.
 * 
 * The HIGH precedence ensures this filter runs early and properly
 * handles preflight OPTIONS requests.
 */
@Configuration
public class CorsConfig {

        @Value("${app.cors.allowed-origins:*}")
        private String allowedOrigins;

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public CorsWebFilter corsWebFilter() {
                CorsConfiguration corsConfig = new CorsConfiguration();

                // Use allowedOriginPatterns allows flexibility (supports *)
                // Split comma-separated values from property
                // Example property:
                // app.cors.allowed-origins=http://localhost:3000,http://localhost:52903
                List<String> origins = Arrays.asList(allowedOrigins.split(","));
                corsConfig.setAllowedOriginPatterns(origins);

                // Allow credentials (required for some auth flows)
                corsConfig.setAllowCredentials(true);

                // Cache preflight response for 1 hour
                corsConfig.setMaxAge(3600L);

                // Allow all common HTTP methods
                corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));

                // Allow all headers in requests
                corsConfig.setAllowedHeaders(List.of("*"));

                // Expose headers to the browser (important for Authorization)
                corsConfig.setExposedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "X-Auth-Token",
                                "Access-Control-Allow-Origin",
                                "Access-Control-Allow-Credentials"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", corsConfig);

                return new CorsWebFilter(source);
        }
}
