package com.taghazout.apigateway.infrastructure.filter;

import com.taghazout.apigateway.application.dto.AuthResponse;
import com.taghazout.apigateway.domain.exception.JwtValidationException;
import com.taghazout.apigateway.domain.service.JwtValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public final class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtValidator jwtValidator;
    private final RouteValidator routeValidator;

    public JwtAuthenticationFilter(JwtValidator jwtValidator, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtValidator = jwtValidator;
        this.routeValidator = routeValidator;

        // Debugging steps
        System.out.println("🟢 JwtAuthenticationFilter CONSTRUCTOR CALLED - Filter is loaded!");

    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            System.out.println("🔵 STEP 1: Filter processing request: " + exchange.getRequest().getPath().value());

            if (!routeValidator.isSecured(exchange.getRequest())) {
                System.out.println("🟢 Open endpoint - skipping auth");
                return chain.filter(exchange);
            }

            System.out.println("🔴 Secured endpoint - checking auth");


            System.out.println("🔴 STEP 4: Secured endpoint - checking authentication");

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            System.out.println("🔵 STEP 5: Authorization header: " + authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                System.out.println("🔴 STEP 6: Missing or invalid Authorization header");
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            System.out.println("🔵 STEP 7: Token extracted (first 10 chars): " +
                    (token.length() > 10 ? token.substring(0, 10) + "..." : token));

            if (token.isEmpty()) {
                System.out.println("🔴 STEP 8: Empty token");
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Empty token");
            }


            System.out.println("🔵 STEP 9: Starting token validation...");

            // Move blocking operation to bounded elastic scheduler
            return Mono.fromCallable(() -> jwtValidator.validateToken(token))
                    .subscribeOn(Schedulers.boundedElastic())
                    .publishOn(Schedulers.parallel()) // Return to parallel for non-blocking work
                    .flatMap(principal -> {
                        ServerWebExchange enriched = enrichExchange(exchange, principal);
                        return chain.filter(enriched);
                    })
                    .onErrorResume(error -> {
                        if (error instanceof JwtValidationException) {
                            System.out.println("🔴 JWT validation failed: " + error.getMessage());
                            return reject(exchange, HttpStatus.UNAUTHORIZED, error.getMessage());
                        } else {
                            // Log internal errors but don't expose details to client
                            return reject(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Authentication service unavailable");
                        }
                    });
        };
    }

    private ServerWebExchange enrichExchange(ServerWebExchange exchange,
                                             com.taghazout.apigateway.domain.model.UserPrincipal principal) {
        return exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.add("X-User-Id", principal.userId());
                    h.add("X-User-Email", principal.email());
                    h.add("X-User-Roles", String.join(",", principal.roles()));
                }))
                .build();
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String reason) {

        // For Debugging purposes
        System.out.println("🔴 REJECTING REQUEST: " + reason);
        System.out.println("🔴 Path: " + exchange.getRequest().getPath().value());


        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        // ✅ Use factory methods for consistent responses
        AuthResponse authResponse;
        if (status == HttpStatus.UNAUTHORIZED) {
            authResponse = AuthResponse.unauthorized(exchange.getRequest().getPath().value());
            byte[] jsonBytes = authResponse.toJsonBytes();

            System.out.println("🔴 JSON Response: " + new String(jsonBytes));
        } else if (status == HttpStatus.FORBIDDEN) {
            authResponse = AuthResponse.forbidden(exchange.getRequest().getPath().value());
        } else {
            authResponse = AuthResponse.of(reason, exchange.getRequest().getPath().value(), status.value());
        }

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                        authResponse.toJsonBytes()
                ))
        );
    }




    public static class Config {

        public Config() {}
        public Config(Config config) {
        }
        // Optional: Add configuration properties here if needed later
    }
}