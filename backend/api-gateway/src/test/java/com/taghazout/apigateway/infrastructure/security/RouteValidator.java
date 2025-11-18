package com.taghazout.apigateway.infrastructure.security;

import com.taghazout.apigateway.infrastructure.filter.RouteValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteValidatorTest {

    private RouteValidator routeValidator;

    @BeforeEach
    void setUp() {
        // Arrange: Create validator with test open endpoints
        List<String> openEndpoints = List.of("/auth/login", "/auth/register", "/public/**");
        routeValidator = new RouteValidator(openEndpoints);
    }

    @Test
    void shouldReturnFalseForOpenEndpoints() {
        // Act & Assert: Open endpoints should return false (not secured)
        ServerHttpRequest request = MockServerHttpRequest.get("/auth/login").build();
        assertFalse(routeValidator.isSecured().test(request));
    }

    @Test
    void shouldReturnTrueForSecuredRoutes() {
        // Act & Assert: Other endpoints should return true (secured)
        ServerHttpRequest request = MockServerHttpRequest.get("/api/users").build();
        assertTrue(routeValidator.isSecured().test(request));
    }

    @Test
    void shouldHandlePathTraversalSafely() {
        // Act & Assert: Malicious paths should still be secured
        ServerHttpRequest request = MockServerHttpRequest.get("/auth/../api/secret").build();
        assertTrue(routeValidator.isSecured().test(request));
    }

    @Test
    void shouldHandleNullRequest() {
        assertThrows(NullPointerException.class, () -> {
            routeValidator.isSecured().test(null);
        });
    }

    @Test
    void shouldHandleEmptyOpenEndpoints() {
        RouteValidator validator = new RouteValidator(List.of());
        ServerHttpRequest request = MockServerHttpRequest.get("/any-path").build();
        assertTrue(validator.isSecured().test(request));
    }
}
