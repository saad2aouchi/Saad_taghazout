package com.taghazout.apigateway.infrastructure.filter;

import com.taghazout.apigateway.domain.exception.JwtValidationException;
import com.taghazout.apigateway.domain.model.UserPrincipal;
import com.taghazout.apigateway.domain.service.JwtValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 * 
 * Tests the JWT validation flow in the Gateway filter,
 * including token extraction, validation, and header forwarding.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private RouteValidator routeValidator;

    @Mock
    private GatewayFilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private GatewayFilter gatewayFilter;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid";
    private static final String USER_ID = "user-123";
    private static final String USER_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtValidator, routeValidator);
        gatewayFilter = jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config());
    }

    // === Open Route Tests ===

    @Test
    @DisplayName("Should skip authentication for open endpoints")
    void shouldSkipAuthForOpenEndpoints() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(routeValidator.isSecured(any())).thenReturn(false);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // When
        Mono<Void> result = gatewayFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(routeValidator).isSecured(any());
        verify(jwtValidator, never()).validateToken(any());
        verify(filterChain).filter(exchange);
    }

    // === Missing Token Tests ===

    @Test
    @DisplayName("Should return 401 when Authorization header is missing")
    void shouldReturn401WhenNoAuthHeader() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/listings")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(routeValidator.isSecured(any())).thenReturn(true);

        // When
        Mono<Void> result = gatewayFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(jwtValidator, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should return 401 when Authorization header doesn't start with Bearer")
    void shouldReturn401WhenInvalidAuthHeaderFormat() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/listings")
                .header(HttpHeaders.AUTHORIZATION, "Basic sometoken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(routeValidator.isSecured(any())).thenReturn(true);

        // When
        Mono<Void> result = gatewayFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return 401 when token is empty after Bearer")
    void shouldReturn401WhenEmptyToken() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/listings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(routeValidator.isSecured(any())).thenReturn(true);

        // When
        Mono<Void> result = gatewayFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // === Valid Token Tests ===

    @Test
    @DisplayName("Should forward request with X-User headers when token is valid")
    void shouldForwardWithUserHeadersWhenValidToken() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/listings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        UserPrincipal principal = new UserPrincipal(
                USER_ID,
                USER_EMAIL,
                Set.of("USER"),
                System.currentTimeMillis() + 900000);

        when(routeValidator.isSecured(any())).thenReturn(true);
        when(jwtValidator.validateToken(VALID_TOKEN)).thenReturn(principal);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // When
        Mono<Void> result = gatewayFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtValidator).validateToken(VALID_TOKEN);
        verify(filterChain).filter(argThat(ex -> {
            // Verify X-User headers were added
            String userId = ex.getRequest().getHeaders().getFirst("X-User-Id");
            String userEmail = ex.getRequest().getHeaders().getFirst("X-User-Email");
            return USER_ID.equals(userId) && USER_EMAIL.equals(userEmail);
        }));
    }

    // === Invalid Token Tests ===

    @Test
    @DisplayName("Should return 401 when token validation fails")
    void shouldReturn401WhenTokenValidationFails() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/listings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(routeValidator.isSecured(any())).thenReturn(true);
        when(jwtValidator.validateToken("invalid-token"))
                .thenThrow(new JwtValidationException("Token expired"));

        // When
        Mono<Void> result = gatewayFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("Should return 500 for unexpected errors during validation")
    void shouldReturn500ForUnexpectedErrors() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/listings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(routeValidator.isSecured(any())).thenReturn(true);
        when(jwtValidator.validateToken(VALID_TOKEN))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        Mono<Void> result = gatewayFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
