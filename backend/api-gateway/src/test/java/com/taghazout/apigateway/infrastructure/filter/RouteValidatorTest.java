package com.taghazout.apigateway.infrastructure.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RouteValidator.
 * 
 * Tests the path matching logic that determines which routes
 * require JWT authentication and which are open/public.
 */
@DisplayName("RouteValidator Tests")
class RouteValidatorTest {

    private static final List<String> OPEN_ENDPOINTS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/actuator/health");

    private RouteValidator routeValidator;

    @BeforeEach
    void setUp() {
        routeValidator = new RouteValidator(OPEN_ENDPOINTS);
    }

    // === Open Endpoint Tests ===

    @Test
    @DisplayName("Should return false (not secured) for /api/v1/auth/login")
    void shouldNotSecureLoginEndpoint() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/login")
                .build();

        // When
        boolean isSecured = routeValidator.isSecured(request);

        // Then
        assertThat(isSecured).isFalse();
    }

    @Test
    @DisplayName("Should return false (not secured) for /api/v1/auth/register")
    void shouldNotSecureRegisterEndpoint() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/register")
                .build();

        // When
        boolean isSecured = routeValidator.isSecured(request);

        // Then
        assertThat(isSecured).isFalse();
    }

    @Test
    @DisplayName("Should return false (not secured) for /actuator/health")
    void shouldNotSecureActuatorHealth() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();

        // When
        boolean isSecured = routeValidator.isSecured(request);

        // Then
        assertThat(isSecured).isFalse();
    }

    // === Secured Endpoint Tests ===

    @Test
    @DisplayName("Should return true (secured) for /api/v1/listings")
    void shouldSecureListingsEndpoint() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/listings")
                .build();

        // When
        boolean isSecured = routeValidator.isSecured(request);

        // Then
        assertThat(isSecured).isTrue();
    }

    @Test
    @DisplayName("Should return true (secured) for /api/v1/bookings")
    void shouldSecureBookingsEndpoint() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/bookings")
                .build();

        // When
        boolean isSecured = routeValidator.isSecured(request);

        // Then
        assertThat(isSecured).isTrue();
    }

    @Test
    @DisplayName("Should return true (secured) for /api/v1/users/profile")
    void shouldSecureUserProfileEndpoint() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/profile")
                .build();

        // When
        boolean isSecured = routeValidator.isSecured(request);

        // Then
        assertThat(isSecured).isTrue();
    }

    // === Edge Cases ===

    @Test
    @DisplayName("Should handle path with trailing slash")
    void shouldHandleTrailingSlash() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/login/")
                .build();

        // When
        boolean isSecured = routeValidator.isSecured(request);

        // Then - should still match open endpoint
        assertThat(isSecured).isFalse();
    }

    @Test
    @DisplayName("Should handle path with multiple slashes")
    void shouldHandleMultipleSlashes() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1//auth//login")
                .build();

        // When
        boolean isSecured = routeValidator.isSecured(request);

        // Then - normalized path should match
        assertThat(isSecured).isFalse();
    }

    @Test
    @DisplayName("Should secure all routes when open endpoints list is empty")
    void shouldSecureAllWhenNoOpenEndpoints() {
        // Given
        RouteValidator emptyValidator = new RouteValidator(Collections.emptyList());
        ServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/login")
                .build();

        // When
        boolean isSecured = emptyValidator.isSecured(request);

        // Then
        assertThat(isSecured).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for null request")
    void shouldThrowExceptionForNullRequest() {
        // When/Then
        assertThatThrownBy(() -> routeValidator.isSecured(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Request cannot be null");
    }

    @Test
    @DisplayName("Should return immutable list from getOpenEndpoints")
    void shouldReturnImmutableOpenEndpoints() {
        // When
        List<String> endpoints = routeValidator.getOpenEndpoints();

        // Then
        assertThatThrownBy(() -> endpoints.add("/new/endpoint"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
