package com.taghazout.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // ← Tells Spring: "This class contains configuration beans"
public class GatewayConfig {

    @Bean  // ← Tells Spring: "Create this object and manage it in the container"
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()  // ← Start building route definitions
                // Route 1: All /auth/** requests → AUTH-SERVICE
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("lb://AUTH-SERVICE"))  // ← "lb://" = Load balance via Eureka

                // Route 2: All /listings/** requests → LISTING-SERVICE
                .route("listing-service", r -> r.path("/listings/**")
                        .uri("lb://LISTING-SERVICE"))

                // Route 3: All /bookings/** requests → BOOKING-SERVICE
                .route("booking-service", r -> r.path("/bookings/**")
                        .uri("lb://BOOKING-SERVICE"))
                .build();  // ← Finalize and create the RouteLocator
    }
}