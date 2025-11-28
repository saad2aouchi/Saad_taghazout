package com.taghazout.apigateway.config;

import com.taghazout.apigateway.infrastructure.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration  // ← Tells Spring: "This class contains configuration beans"
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    @Bean  // ← Tells Spring: "Create this object and manage it in the container"
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()  // ← Start building route definitions
                // Route 1: All /auth/** requests → AUTH-SERVICE
                // === Public Routes (No JWT) ===
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri("lb://auth-service"))  // ← "lb://" = Load balance via Eureka

                // Route 2: All /listings/** requests → LISTING-SERVICE
                // === Secured Routes (JWT Required) ===
                .route("listing-service", r -> r.path("/api/v1/listings/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(JwtAuthenticationFilter.Config::new))) // ✅ ATTACH FILTER
                        .uri("lb://listing-service"))

                // Route 3: All /bookings/** requests → BOOKING-SERVICE
                .route("booking-service", r -> r.path("/api/v1/bookings/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(JwtAuthenticationFilter.Config::new))) // ✅ ATTACH FILTER
                        .uri("lb://booking-service"))

                // === Discovery & Config (Public) ===
                .route("eureka", r -> r.path("/eureka/**")
                        .filters(f -> f.stripPrefix(1))  // Strips /eureka, so /eureka/web -> /web
                        .uri("http://localhost:8762"))

                .route("actuator", r -> r.path("/actuator/**")
                        .filters(f -> f.stripPrefix(1))  // Strips /actuator, so /actuator/gateway -> /web
                        .uri("http://localhost:8762"))

                .route("config", r -> r.path("/config/**")
                        .uri("lb://config-server"))

                .build();
    }
}