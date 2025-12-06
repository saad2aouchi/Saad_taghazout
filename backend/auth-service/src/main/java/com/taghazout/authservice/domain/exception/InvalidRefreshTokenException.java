package com.taghazout.authservice.domain.exception;

/**
 * Exception thrown when a refresh token is invalid or cannot be used.
 * 
 * Reasons include:
 * - Token not found in database
 * - Token expired
 * - Token revoked
 * - Token format invalid
 * 
 * HTTP Status: 401 Unauthorized
 */
public class InvalidRefreshTokenException extends RuntimeException {

    private final String token;

    public InvalidRefreshTokenException(String message) {
        super(message);
        this.token = null;
    }

    public InvalidRefreshTokenException(String message, String token) {
        super(message);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
