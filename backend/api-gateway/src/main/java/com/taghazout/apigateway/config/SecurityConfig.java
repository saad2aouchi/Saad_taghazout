package com.taghazout.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Reactive Security Configuration for API Gateway.
 * 
 * SOLID Principles:
 * - SRP: ONLY configures reactive security – no routes, no JWT logic.
 * - OCP: New security rules added via new SecurityWebFilterChain beans.
 * - DIP: Returns SecurityWebFilterChain abstraction.
 * 
 * Design Decision:
 * Spring Security permits ALL exchanges. Authentication is handled by:
 * - JwtAuthenticationFilter: Validates JWT tokens
 * - RouteValidator: Determines which routes require authentication
 * 
 * This separation keeps security configuration simple while delegating
 * the complex JWT logic to dedicated components.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Configures reactive security to permit all exchanges.
     * 
     * @Order(LOWEST_PRECEDENCE) ensures Gateway filters run FIRST,
     *                           allowing JwtAuthenticationFilter to handle
     *                           authentication.
     * 
     *                           CSRF is disabled for stateless JWT authentication.
     */
    @Order(Ordered.LOWEST_PRECEDENCE)
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll())
                .build();
    }
}
