package com.taghazout.authservice.infrastructure.adapter;

import com.taghazout.authservice.domain.entity.User;
import com.taghazout.authservice.domain.port.TokenProviderPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Provider implementation using JJWT library.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for JWT token operations
 * - DIP: Implements TokenProviderPort interface (domain depends on abstraction)
 * - OCP: Can be extended with additional claims or token types
 * 
 * Token Configuration:
 * - Algorithm: HMAC-SHA256 (HS256)
 * - Secret: Loaded from application.properties (jwt.secret)
 * - Access Token: 15 minutes expiration
 * - Refresh Token: 7 days expiration (stored in database for revocation)
 * 
 * Claims Structure:
 * - sub (subject): User email
 * - userId: User ID (for quick lookups)
 * - iat (issued at): Token creation timestamp
 * - exp (expiration): Token expiration timestamp
 * 
 * Security Notes:
 * - Secret must be at least 256 bits (32 characters)
 * - Tokens are signed (integrity verified)
 * - Tokens are NOT encrypted (don't put sensitive data)
 * - Always validate tokens before trusting claims
 */
@Component
public class JwtTokenProvider implements TokenProviderPort {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    /**
     * Constructor with JWT configuration from properties.
     * 
     * @param secret                 JWT secret key (min 32 characters)
     * @param accessTokenExpiration  access token expiration in milliseconds
     * @param refreshTokenExpiration refresh token expiration in milliseconds
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Override
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration);
    }



    @Override
    public String validateTokenAndGetEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject(); // Subject is the email
    }

    @Override
    public Long getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // If we can't parse it, consider it expired
        }
    }

    /**
     * Generates a JWT token for a user.
     * 
     * @param user         the user to generate token for
     * @param expirationMs expiration time in milliseconds
     * @return JWT token string
     */
    private String generateToken(User user, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();

        // Only add userId if user has been persisted (ID is not null)
        if (user.getId() != null) {
            claims.put("userId", user.getId());
        }
        claims.put("email", user.getEmail());

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail()) // Subject claim
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS256) // Explicitly use HS256
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration);
    }

    /**
     * Extracts all claims from a JWT token.
     * 
     * Validates signature and expiration during parsing.
     * 
     * @param token the JWT token
     * @return Claims object containing all token claims
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // Verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
