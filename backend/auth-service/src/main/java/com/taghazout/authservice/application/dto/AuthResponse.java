package com.taghazout.authservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for authentication response.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for auth response data
 * - Immutable: Thread-safe
 * 
 * Returned after:
 * - Successful registration (201 Created)
 * - Successful login (200 OK)
 * - Successful token refresh (200 OK)
 * 
 * Contains:
 * - User information (email, name)
 * - JWT tokens (access + refresh)
 * 
 * JSON Example:
 * {
 * "email": "user@example.com",
 * "firstName": "John",
 * "lastName": "Doe",
 * "accessToken": "eyJ...",
 * "refreshToken": "eyJ..."
 * }
 */
public record AuthResponse(

        String email,

        @JsonProperty("firstName") String firstName,

        @JsonProperty("lastName") String lastName,

        @JsonProperty("accessToken") String accessToken,

        @JsonProperty("refreshToken") String refreshToken) {
    /**
     * Compact constructor for validation.
     */
    public AuthResponse {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be null or blank");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be null or blank");
        }
    }

    /**
     * Factory method for creating response from tokens and user data.
     */
    public static AuthResponse of(String email, String firstName, String lastName,
            String accessToken, String refreshToken) {
        return new AuthResponse(email, firstName, lastName, accessToken, refreshToken);
    }

    /**
     * Factory method for creating response from tokens and email only.
     */
    public static AuthResponse of(String email, String accessToken, String refreshToken) {
        return new AuthResponse(email, null, null, accessToken, refreshToken);
    }
}
