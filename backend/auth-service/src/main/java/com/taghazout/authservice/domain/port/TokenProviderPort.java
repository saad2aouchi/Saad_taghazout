package com.taghazout.authservice.domain.port;

import com.taghazout.authservice.domain.entity.User;

/**
 * Port (interface) for JWT token generation and validation.
 * 
 * SOLID Principles:
 * - DIP: Application layer depends on this abstraction
 * - ISP: Minimal interface focused only on token operations
 * - SRP: Only responsible for token contract
 * 
 * Design Pattern: Hexagonal Architecture (Port)
 * 
 * The actual implementation (JwtTokenProvider) will be in the infrastructure
 * layer,
 * using JJWT library. This keeps domain/application layers independent of
 * the JWT implementation details.
 */
public interface TokenProviderPort {

    /**
     * Generates a JWT access token for a user.
     * 
     * Access tokens are short-lived (typically 15 minutes)
     * and used for API authentication.
     * 
     * @param user the user for whom to generate the token
     * @return JWT access token string
     */
    String generateAccessToken(User user);

    /**
     * Generates a JWT refresh token for a user.
     * 
     * Refresh tokens are long-lived (typically 7 days)
     * and used to obtain new access tokens.
     * 
     * @param user the user for whom to generate the token
     * @return JWT refresh token string
     */
    String generateRefreshToken(User user);

    /**
     * Validates a JWT token and extracts the user email.
     * 
     * @param token the JWT token to validate
     * @return the email claim from the token
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    String validateTokenAndGetEmail(String token);

    /**
     * Extracts user ID from a JWT token without full validation.
     * 
     * Useful for quick lookups when signature validation isn't required.
     * 
     * @param token the JWT token
     * @return the user ID claim
     */
    Long getUserIdFromToken(String token);

    /**
     * Checks if a token is expired.
     * 
     * @param token the JWT token
     * @return true if token is expired
     */
    boolean isTokenExpired(String token);
}
