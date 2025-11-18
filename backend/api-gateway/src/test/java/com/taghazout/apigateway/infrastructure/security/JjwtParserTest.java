package com.taghazout.apigateway.infrastructure.security;

import com.taghazout.apigateway.domain.exception.JwtValidationException;
import com.taghazout.apigateway.domain.model.UserPrincipal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JjwtParserTest {

    // 👇 TEST FIELDS
    private JjwtParser jwtParser;
    private SecretKey testKey;
    private String validToken;

    // 👇 SETUP - Create test JWT token
    @BeforeEach
    void setUp() {
        // ARRANGE: Create a test secret (same as in real application)
        String secret = "test-secret-key-32-chars-long-here-123";
        testKey = Keys.hmacShaKeyFor(secret.getBytes());

        // Create the parser with the same secret
        jwtParser = new JjwtParser(secret);

        // 👇 Create a VALID test JWT token
        validToken = Jwts.builder()
                .setSubject("user123")                    // ← User ID
                .claim("email", "test@example.com")       // ← Custom claim
                .claim("roles", "USER,ADMIN")             // ← Custom claim
                .setExpiration(new Date(System.currentTimeMillis() + 100000)) // ← Future expiration
                .signWith(testKey)                        // ← Sign with our key
                .compact();                               // ← Build the token string
    }

    // 👇 TEST 1: Parse valid token successfully
    @Test
    void shouldParseValidToken() {
        // ACT: Parse the token we created in setup
        UserPrincipal principal = jwtParser.parse(validToken);

        // ASSERT: Verify all fields are extracted correctly
        assertNotNull(principal, "Principal should not be null");
        assertEquals("user123", principal.userId(), "User ID should match");
        assertEquals("test@example.com", principal.email(), "Email should match");
        assertTrue(principal.roles().contains("USER"), "Should contain USER role");
        assertTrue(principal.roles().contains("ADMIN"), "Should contain ADMIN role");
    }

    // 👇 TEST 2: Handle invalid token
    @Test
    void shouldThrowExceptionForInvalidToken() {
        // ACT & ASSERT: Try to parse garbage token
        assertThrows(JwtValidationException.class, () -> {
            jwtParser.parse("this.is.not.a.valid.jwt.token");
        });
        // 👆 This expects the method to THROW JwtValidationException
    }

    // 👇 TEST 3: Handle expired token
    @Test
    void shouldThrowExceptionForExpiredToken() {
        // ARRANGE: Create an EXPIRED token
        String expiredToken = Jwts.builder()
                .setSubject("user123")
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // ← PAST date
                .signWith(testKey)
                .compact();

        // ACT & ASSERT: Should throw exception for expired token
        assertThrows(JwtValidationException.class, () -> {
            jwtParser.parse(expiredToken);
        });
    }

    // 👇 TEST 4: Handle token with wrong signature
    @Test
    void shouldThrowExceptionForWrongSignature() {
        // ARRANGE: Create token with DIFFERENT secret
        String differentSecret = "different-secret-32-chars-long-here";
        String wrongSignatureToken = Jwts.builder()
                .setSubject("user123")
                .signWith(Keys.hmacShaKeyFor(differentSecret.getBytes()))
                .compact();

        // ACT & ASSERT: Should throw exception (signature verification fails)
        assertThrows(JwtValidationException.class, () -> {
            jwtParser.parse(wrongSignatureToken);
        });
    }
}
