package com.taghazout.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * SRP: ONLY configures reactive security – no routes, no JWT logic.
 * OCP: New security rules added via new SecurityWebFilterChain beans.
 * DIP: Returns SecurityWebFilterChain abstraction.
 */


@Configuration
//Enables reactive (non-blocking) security. This is not Spring Security MVC – it's a completely different stack for WebFlux.
@EnableWebFluxSecurity
public class SecurityConfig {


//    Dependency Injection: The filter is provided by Spring (DIP). final ensures it's set once and never changed (immutability).*
    private final String[] permittedEndpoints;

//    @Value injects the array from properties. .clone() prevents external modifications (defensive programming).*
    public SecurityConfig(@Value("${gateway.open-endpoints}") String[] permittedEndpoints) {
        this.permittedEndpoints = permittedEndpoints.clone(); // Defensive copy
    }

//    SecurityWebFilterChain – reactive equivalent of SecurityFilterChain in servlet world.
//            ServerHttpSecurity – builder for reactive security rules.*
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable()) // Stateless JWT – no CSRF
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(permittedEndpoints).permitAll()
                        .anyExchange().authenticated() // All other routes need JWT
                )
//        authorizeExchange – reactive version of authorizeRequests.
//.permitAll() – allows traffic to /api/v1/auth/** without authentication.
//         .authenticated() – blocks everything else unless JWT filter adds authentication context.*
                .build();
    }
}