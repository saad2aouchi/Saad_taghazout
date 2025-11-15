package com.taghazout.apigateway.infrastructure.filter;

import com.taghazout.apigateway.application.dto.AuthResponse;
import com.taghazout.apigateway.domain.exception.JwtValidationException;
import com.taghazout.apigateway.domain.service.JwtValidator;
import jakarta.ws.rs.core.HttpHeaders;
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
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Use method instead of field + remove negate() for clarity
            if (!routeValidator.isSecured().test(exchange.getRequest())) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            if (token.isEmpty()) {
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Empty token");
            }

            // Move blocking operation to bounded elastic scheduler
            return Mono.fromCallable(() -> jwtValidator.validateToken(token))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(principal -> {
                        ServerWebExchange enriched = enrichExchange(exchange, principal);
                        return chain.filter(enriched);
                    })
                    .onErrorResume(error -> {
                        if (error instanceof JwtValidationException) {
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
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        AuthResponse authResponse = AuthResponse.of(reason,
                exchange.getRequest().getPath().value(),
                status.value());

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                        authResponse.toJsonBytes()
                ))
        );
    }

    public static class Config {
        // Optional: Add configuration properties here if needed later
    }
}