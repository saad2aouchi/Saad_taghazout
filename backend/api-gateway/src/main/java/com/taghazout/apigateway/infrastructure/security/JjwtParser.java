package com.taghazout.apigateway.infrastructure.security;

import com.taghazout.apigateway.domain.exception.JwtValidationException;
import com.taghazout.apigateway.domain.model.UserPrincipal;
import com.taghazout.apigateway.domain.service.JwtParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Collections;
import java.util.Set;


/**
 * SRP: ONLY parses and verifies JWT signature & claims.
 * No blacklist, no Redis, no secret management.
 */
@Component
public class JjwtParser implements JwtParser {

    private final SecretKey key;

    JjwtParser(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public UserPrincipal parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            if (userId == null || userId.isBlank()) {
                throw new JwtValidationException("Missing 'sub' claim in token");
            }

            String email = claims.get("email", String.class);

            String rolesStr = claims.get("roles", String.class);
            Set<String> roles;
            if (rolesStr == null || rolesStr.isBlank()) {
                roles = Collections.singleton("USER"); // Default role
            } else {
                roles = Set.of(rolesStr.split(","));
            }

            if (claims.getExpiration() == null) {
                throw new JwtValidationException("Missing 'exp' claim in token");
            }

            return new UserPrincipal(
                    userId,
                    email,
                    roles,
                    claims.getExpiration().getTime()
            );
        } catch (ExpiredJwtException ex) {
            throw new JwtValidationException("Token has expired", ex);
        } catch (SignatureException ex) {
            throw new JwtValidationException("Invalid token signature - check secret key", ex);
        } catch (MalformedJwtException ex) {
            throw new JwtValidationException("Malformed JWT token", ex);
        } catch (JwtValidationException ex) {
            throw ex; // Re-throw our own exceptions
        } catch (Exception ex) {
            System.err.println("[JjwtParser] Unexpected error parsing token: " + ex.getClass().getName() + " - " + ex.getMessage());
            throw new JwtValidationException("JWT parsing failed: " + ex.getMessage(), ex);
        }
    }
}




