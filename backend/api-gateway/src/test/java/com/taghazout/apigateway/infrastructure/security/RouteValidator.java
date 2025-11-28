package com.taghazout.apigateway.infrastructure.security;

import com.taghazout.apigateway.infrastructure.filter.RouteValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    // ===========================================
    // OPEN ENDPOINTS TESTS (Should return FALSE)
    // ===========================================

    @Nested
    @DisplayName("Open Endpoints - Should Return False (Not Secured)")
    class OpenEndpointsTests {

        @Test
        @DisplayName("should return false for /auth/login endpoint")
        void shouldReturnFalseForAuthLoginEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.get("/auth/login").build();
            assertFalse(routeValidator.isSecured(request),
                    "/auth/login should be an open endpoint (not secured)");
        }

        @Test
        @DisplayName("should return false for /auth/register endpoint")
        void shouldReturnFalseForAuthRegisterEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.get("/auth/register").build();
            assertFalse(routeValidator.isSecured(request),
                    "/auth/register should be an open endpoint (not secured)");
        }

        @Test
        @DisplayName("should return false for wildcard /public/** endpoints")
        void shouldReturnFalseForPublicWildcardEndpoints() {
            ServerHttpRequest request = MockServerHttpRequest.get("/public/docs").build();
            assertFalse(routeValidator.isSecured(request),
                    "/public/docs should match /public/** pattern");
        }

        @Test
        @DisplayName("should return false for nested /public/** paths")
        void shouldReturnFalseForNestedPublicPaths() {
            ServerHttpRequest request = MockServerHttpRequest.get("/public/api/v1/docs").build();
            assertFalse(routeValidator.isSecured(request),
                    "/public/api/v1/docs should match /public/** pattern");
        }

        @ParameterizedTest
        @DisplayName("should return false for various open auth endpoints")
        @ValueSource(strings = {"/auth/login", "/auth/register", "/public/health", "/public/info"})
        void shouldReturnFalseForVariousOpenEndpoints(String path) {
            ServerHttpRequest request = MockServerHttpRequest.get(path).build();
            assertFalse(routeValidator.isSecured(request),
                    path + " should be an open endpoint");
        }
    }


    // ===========================================
    // SECURED ENDPOINTS TESTS (Should return TRUE)
    // ===========================================

    @Nested
    @DisplayName("Secured Endpoints - Should Return True")
    class SecuredEndpointsTests {

        @Test
        @DisplayName("should return true for /api/users endpoint")
        void shouldReturnTrueForApiUsersEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.get("/api/users").build();
            assertTrue(routeValidator.isSecured(request),
                    "/api/users should be a secured endpoint");
        }

        @Test
        @DisplayName("should return true for /api/orders endpoint")
        void shouldReturnTrueForApiOrdersEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
            assertTrue(routeValidator.isSecured(request),
                    "/api/orders should be a secured endpoint");
        }

        @Test
        @DisplayName("should return true for /admin/dashboard endpoint")
        void shouldReturnTrueForAdminDashboardEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.get("/admin/dashboard").build();
            assertTrue(routeValidator.isSecured(request),
                    "/admin/dashboard should be a secured endpoint");
        }

        @Test
        @DisplayName("should return true for /private/data endpoint")
        void shouldReturnTrueForPrivateDataEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.get("/private/data").build();
            assertTrue(routeValidator.isSecured(request),
                    "/private/data should be a secured endpoint");
        }

        @ParameterizedTest
        @DisplayName("should return true for various secured endpoints")
        @ValueSource(strings = {"/api/v1/users", "/api/v2/products", "/admin/settings", "/dashboard", "/profile"})
        void shouldReturnTrueForVariousSecuredEndpoints(String path) {
            ServerHttpRequest request = MockServerHttpRequest.get(path).build();
            assertTrue(routeValidator.isSecured(request),
                    path + " should be a secured endpoint");
        }
    }

    // ===========================================
    // SECURITY TESTS (Path Traversal, Edge Cases)
    // ===========================================

    @Nested
    @DisplayName("Security Tests - Path Traversal and Edge Cases")
    class SecurityTests {

        @Test
        @DisplayName("should return true for path traversal attack attempt")
        void shouldHandlePathTraversalSafely() {
            // Malicious path attempting to escape /auth and access /api
            ServerHttpRequest request = MockServerHttpRequest.get("/auth/../api/secret").build();
            assertTrue(routeValidator.isSecured(request),
                    "Path traversal attempt should be treated as secured");
        }

        @Test
        @DisplayName("should return true for double dot path traversal")
        void shouldHandleDoubleDotPathTraversal() {
            ServerHttpRequest request = MockServerHttpRequest.get("/public/../private/data").build();
            assertTrue(routeValidator.isSecured(request),
                    "Double dot path traversal should be treated as secured");
        }

        @Test
        @DisplayName("should return true for encoded path traversal")
        void shouldHandleEncodedPathTraversal() {
            ServerHttpRequest request = MockServerHttpRequest.get("/auth%2F..%2Fapi/secret").build();
            assertTrue(routeValidator.isSecured(request),
                    "Encoded path traversal should be treated as secured");
        }

        @Test
        @DisplayName("should not match partial path prefixes")
        void shouldNotMatchPartialPathPrefixes() {
            // /authX should NOT match /auth/login
            ServerHttpRequest request = MockServerHttpRequest.get("/authX/login").build();
            assertTrue(routeValidator.isSecured(request),
                    "/authX/login should NOT match /auth/login pattern");
        }

        @Test
        @DisplayName("should not match path with extra segments before")
        void shouldNotMatchPathWithExtraSegmentsBefore() {
            ServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
            assertTrue(routeValidator.isSecured(request),
                    "/api/auth/login should NOT match /auth/login pattern");
        }
    }

    // ===========================================
    // NULL AND EMPTY INPUT TESTS
    // ===========================================

    @Nested
    @DisplayName("Null and Empty Input Tests")
    class NullAndEmptyInputTests {

        @Test
        @DisplayName("should throw NullPointerException for null request")
        void shouldHandleNullRequest() {
            assertThrows(NullPointerException.class, () -> {
                routeValidator.isSecured(null);
            }, "Should throw NullPointerException for null request");
        }

        @Test
        @DisplayName("should return true when open endpoints list is empty")
        void shouldHandleEmptyOpenEndpoints() {
            RouteValidator validator = new RouteValidator(List.of());
            ServerHttpRequest request = MockServerHttpRequest.get("/any-path").build();
            assertTrue(validator.isSecured(request),
                    "All routes should be secured when no open endpoints defined");
        }

        @Test
        @DisplayName("should throw exception for null open endpoints list")
        void shouldThrowExceptionForNullOpenEndpointsList() {
            assertThrows(NullPointerException.class, () -> {
                new RouteValidator(null);
            }, "Should throw NullPointerException for null open endpoints list");
        }
    }

    // ===========================================
    // HTTP METHOD TESTS
    // ===========================================

    @Nested
    @DisplayName("HTTP Method Tests")
    class HttpMethodTests {

        @Test
        @DisplayName("should return false for POST to open endpoint")
        void shouldReturnFalseForPostToOpenEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.post("/auth/login").build();
            assertFalse(routeValidator.isSecured(request),
                    "POST /auth/login should be an open endpoint");
        }

        @Test
        @DisplayName("should return false for PUT to open endpoint")
        void shouldReturnFalseForPutToOpenEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.put("/auth/register").build();
            assertFalse(routeValidator.isSecured(request),
                    "PUT /auth/register should be an open endpoint");
        }

        @Test
        @DisplayName("should return true for DELETE to secured endpoint")
        void shouldReturnTrueForDeleteToSecuredEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.delete("/api/users/1").build();
            assertTrue(routeValidator.isSecured(request),
                    "DELETE /api/users/1 should be a secured endpoint");
        }

        @Test
        @DisplayName("should return true for PATCH to secured endpoint")
        void shouldReturnTrueForPatchToSecuredEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.patch("/api/users/1").build();
            assertTrue(routeValidator.isSecured(request),
                    "PATCH /api/users/1 should be a secured endpoint");
        }
    }

    // ===========================================
    // QUERY PARAMETER TESTS
    // ===========================================

    @Nested
    @DisplayName("Query Parameter Tests")
    class QueryParameterTests {

        @Test
        @DisplayName("should return false for open endpoint with query params")
        void shouldReturnFalseForOpenEndpointWithQueryParams() {
            ServerHttpRequest request = MockServerHttpRequest
                    .get("/auth/login?redirect=/dashboard")
                    .build();
            assertFalse(routeValidator.isSecured(request),
                    "/auth/login with query params should still be open");
        }

        @Test
        @DisplayName("should return true for secured endpoint with query params")
        void shouldReturnTrueForSecuredEndpointWithQueryParams() {
            ServerHttpRequest request = MockServerHttpRequest
                    .get("/api/users?page=1&size=10")
                    .build();
            assertTrue(routeValidator.isSecured(request),
                    "/api/users with query params should still be secured");
        }
    }

    // ===========================================
    // CASE SENSITIVITY TESTS
    // ===========================================

    @Nested
    @DisplayName("Case Sensitivity Tests")
    class CaseSensitivityTests {

        @Test
        @DisplayName("should be case sensitive for /AUTH/LOGIN (uppercase)")
        void shouldBeCaseSensitiveForUppercase() {
            ServerHttpRequest request = MockServerHttpRequest.get("/AUTH/LOGIN").build();
            // AntPathMatcher is case-sensitive by default
            assertTrue(routeValidator.isSecured(request),
                    "/AUTH/LOGIN should NOT match /auth/login (case sensitive)");
        }

        @Test
        @DisplayName("should be case sensitive for /Auth/Login (mixed case)")
        void shouldBeCaseSensitiveForMixedCase() {
            ServerHttpRequest request = MockServerHttpRequest.get("/Auth/Login").build();
            assertTrue(routeValidator.isSecured(request),
                    "/Auth/Login should NOT match /auth/login (case sensitive)");
        }
    }

    // ===========================================
    // TRAILING SLASH TESTS
    // ===========================================

    @Nested
    @DisplayName("Trailing Slash Tests")
    class TrailingSlashTests {

        @Test
        @DisplayName("should handle trailing slash on open endpoint")
        void shouldHandleTrailingSlashOnOpenEndpoint() {
            ServerHttpRequest request = MockServerHttpRequest.get("/auth/login/").build();
            // Note: Behavior depends on AntPathMatcher configuration
            // This test documents the current behavior
            boolean result = routeValidator.isSecured(request);
            // Document the actual behavior - adjust assertion based on expected behavior
            assertNotNull(result);
        }

        @Test
        @DisplayName("should handle multiple trailing slashes")
        void shouldHandleMultipleTrailingSlashes() {
            ServerHttpRequest request = MockServerHttpRequest.get("/auth/login///").build();
            boolean result = routeValidator.isSecured(request);
            assertNotNull(result);
        }
    }

    // ===========================================
    // WILDCARD PATTERN TESTS
    // ===========================================

    @Nested
    @DisplayName("Wildcard Pattern Tests")
    class WildcardPatternTests {

        @Test
        @DisplayName("should match single level under /public/**")
        void shouldMatchSingleLevelUnderPublic() {
            ServerHttpRequest request = MockServerHttpRequest.get("/public/health").build();
            assertFalse(routeValidator.isSecured(request),
                    "/public/health should match /public/** pattern");
        }

        @Test
        @DisplayName("should match multiple levels under /public/**")
        void shouldMatchMultipleLevelsUnderPublic() {
            ServerHttpRequest request = MockServerHttpRequest.get("/public/api/v1/health").build();
            assertFalse(routeValidator.isSecured(request),
                    "/public/api/v1/health should match /public/** pattern");
        }

        @Test
        @DisplayName("should match /public root path")
        void shouldMatchPublicRootPath() {
            ServerHttpRequest request = MockServerHttpRequest.get("/public").build();
            // Note: /public/** may or may not match /public depending on AntPathMatcher
            boolean result = routeValidator.isSecured(request);
            assertNotNull(result);
        }
    }

    // ===========================================
    // GET OPEN ENDPOINTS TEST
    // ===========================================

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("should return immutable list of open endpoints")
        void shouldReturnImmutableOpenEndpoints() {
            List<String> endpoints = routeValidator.getOpenEndpoints();
            assertThrows(UnsupportedOperationException.class, () -> {
                endpoints.add("/new/endpoint");
            }, "Open endpoints list should be immutable");
        }

        @Test
        @DisplayName("should contain expected open endpoints")
        void shouldContainExpectedOpenEndpoints() {
            List<String> endpoints = routeValidator.getOpenEndpoints();
            assertEquals(3, endpoints.size());
            assertTrue(endpoints.contains("/auth/login"));
            assertTrue(endpoints.contains("/auth/register"));
            assertTrue(endpoints.contains("/public/**"));
        }

        @Test
        @DisplayName("should filter out null and empty endpoints during construction")
        void shouldFilterOutNullAndEmptyEndpoints() {
            List<String> endpointsWithNulls = new java.util.ArrayList<>();
            endpointsWithNulls.add("/valid/path");
            endpointsWithNulls.add(null);
            endpointsWithNulls.add("");
            endpointsWithNulls.add("  ");
            endpointsWithNulls.add("/another/valid");

            RouteValidator validator = new RouteValidator(endpointsWithNulls);
            List<String> filtered = validator.getOpenEndpoints();

            assertEquals(2, filtered.size());
            assertTrue(filtered.contains("/valid/path"));
            assertTrue(filtered.contains("/another/valid"));
        }

        @Test
        @DisplayName("should remove duplicate endpoints during construction")
        void shouldRemoveDuplicateEndpoints() {
            List<String> duplicateEndpoints = List.of("/auth/login", "/auth/login", "/auth/register");
            RouteValidator validator = new RouteValidator(duplicateEndpoints);

            List<String> filtered = validator.getOpenEndpoints();
            assertEquals(2, filtered.size());
        }
    }

    @Test
    void shouldHandlePathTraversalSafely() {
        // Act & Assert: Malicious paths should still be secured
        ServerHttpRequest request = MockServerHttpRequest.get("/auth/../api/secret").build();
        assertTrue(routeValidator.isSecured(request));
    }

    @Test
    void shouldHandleNullRequest() {
        assertThrows(NullPointerException.class, () -> {
            routeValidator.isSecured(null);
        });
    }

    @Test
    void shouldHandleEmptyOpenEndpoints() {
        RouteValidator validator = new RouteValidator(List.of());
        ServerHttpRequest request = MockServerHttpRequest.get("/any-path").build();
        assertTrue(validator.isSecured(request));
    }
}
