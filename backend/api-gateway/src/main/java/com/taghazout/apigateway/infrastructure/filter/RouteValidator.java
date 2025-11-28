package com.taghazout.apigateway.infrastructure.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Component
public final class RouteValidator {

    private final List<String> openEndpoints;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public RouteValidator(List<String> openEndpoints) {
        this.openEndpoints = validateAndCopy(openEndpoints);
        System.out.println("🟢 RouteValidator loaded with open endpoints: " + openEndpoints);

    }

    /**
     * Encapsulated predicate - cannot be modified externally
     */
    public boolean isSecured(ServerHttpRequest request) {
        Objects.requireNonNull(request, "Request cannot be null ! ");
        String path = getNormalizedPath(request);

        // Early return for empty open endpoints - all routes are secured
        if (openEndpoints.isEmpty()) {
            return true;
        }

        boolean isOpen = openEndpoints.stream()
                .anyMatch(endpoint -> pathMatcher.match(endpoint, path));
        return !isOpen;
    }


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
        String rawPath = Objects.requireNonNull(request.getURI().getPath(), "Path cannot be null");

        // First normalize multiple slashes
        String normalizedSlashes = rawPath.replaceAll("/+", "/");

        String resolvedPath;
        try {
            // Paths.get().normalize() resolves . and .. sequences
            resolvedPath = Paths.get(normalizedSlashes).normalize().toString();

            // Convert backslashes to forward slashes (Windows compatibility)
            resolvedPath = resolvedPath.replace("\\", "/");

            // Ensure path starts with / for web paths
            if (!resolvedPath.startsWith("/")) {
                resolvedPath = "/" + resolvedPath;
            }
        } catch (Exception e) {
            // If path normalization fails, treat as potentially malicious - secure by default
            return "/INVALID_PATH";
        }

        // Remove trailing slash (except for root path)
        if (resolvedPath.length() > 1 && resolvedPath.endsWith("/")) {
            resolvedPath = resolvedPath.substring(0, resolvedPath.length() - 1);
        }

        return resolvedPath;
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

        if (validated.size() != endpoints.size()) {
            System.out.println("⚠️ Filtered out invalid open endpoints");
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
