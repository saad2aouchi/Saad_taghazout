package com.taghazout.authservice.application.dto;

/**
 * DTO containing JWT access and refresh tokens.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for token pair data
 * - Immutable: Thread-safe, defensive design
 * 
 * Usage:
 * - Returned after successful login
 * - Returned after successful token refresh
 * 
 * Token Types:
 * - Access Token: Short-lived (15 min), used for API authentication
 * - Refresh Token: Long-lived (7 days), used to get new access tokens
 */
public record TokenPair(
        String accessToken,
        String refreshToken) {
    /**
     * Compact constructor for validation.
     */
    public TokenPair {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be null or blank");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be null or blank");
        }
    }
}
