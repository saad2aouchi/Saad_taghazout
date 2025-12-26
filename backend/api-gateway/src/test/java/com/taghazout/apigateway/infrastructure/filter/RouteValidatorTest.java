package com.taghazout.apigateway.infrastructure.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RouteValidatorTest {

    private RouteValidator routeValidator;

    @BeforeEach
    void setUp() {
        List<String> openEndpoints = Arrays.asList(
                "/api/v1/auth/login",
                "/api/v1/auth/register",
                "/eureka/**");
        routeValidator = new RouteValidator(openEndpoints);
    }

    @Test
    void shouldIdentifySecuredRoute() {
        ServerHttpRequest request = MockServerHttpRequest.get("/api/v1/listings").build();
        assertThat(routeValidator.isSecured(request)).isTrue();
    }

    @Test
    void shouldIdentifyOpenRoute() {
        ServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        assertThat(routeValidator.isSecured(request)).isFalse();
    }

    @Test
    void shouldIdentifyOpenRouteWithWildcard() {
        ServerHttpRequest request = MockServerHttpRequest.get("/eureka/web").build();
        assertThat(routeValidator.isSecured(request)).isFalse();
    }
}
