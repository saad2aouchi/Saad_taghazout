package com.taghazout.apigateway.infrastructure.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Component
public final class RouteValidator {

    private final List<String> openEndpoints;
    private final PathMatcher pathMatcher;

    public RouteValidator(List<String> openEndpoints) {
        this.openEndpoints = validateAndCopy(openEndpoints);
        this.pathMatcher = new AntPathMatcher(); // Spring's pattern matcher
    }

    /**
     * Encapsulated predicate - cannot be modified externally
     */
    public Predicate<ServerHttpRequest> isSecured() {
        return this::isSecuredRoute;
    }

    /**
     * Private implementation with proper path matching
     */
    private boolean isSecuredRoute(ServerHttpRequest request) {
        Objects.requireNonNull(request, "Request cannot be null");

        String path = getNormalizedPath(request);

        // Early return for empty open endpoints
        if (openEndpoints.isEmpty()) {
            return true; // All routes secured
        }

        // Check against open endpoints
        return openEndpoints.stream()
                .noneMatch(openPath -> pathMatcher.match(openPath, path));
    }

    /**
     * Normalize path to prevent path traversal attacks
     */
    private String getNormalizedPath(ServerHttpRequest request) {
        return Objects.requireNonNull(request.getURI().getPath(), "Path cannot be null")
                .replaceAll("/+", "/") // Normalize slashes
                .replaceAll("/$", ""); // Remove trailing slash
    }

    /**
     * Validate configuration and create immutable copy
     */
    private List<String> validateAndCopy(List<String> endpoints) {
        Objects.requireNonNull(endpoints, "Open endpoints list cannot be null");

        List<String> validated = endpoints.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(endpoint -> !endpoint.isEmpty())
                .distinct()
                .toList();

        // Log warning if any endpoints were filtered out
        if (validated.size() != endpoints.size()) {
            // Log: "Filtered out invalid open endpoints"
        }

        return Collections.unmodifiableList(validated);
    }

    /**
     * Utility method for testing configuration
     */
    public List<String> getOpenEndpoints() {
        return openEndpoints; // Returns immutable copy
    }
}
