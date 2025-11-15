package com.taghazout.apigateway.infrastructure.security;

import com.taghazout.apigateway.domain.exception.JwtValidationException;
import com.taghazout.apigateway.domain.model.UserPrincipal;
import com.taghazout.apigateway.domain.service.JwtParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
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
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return new UserPrincipal(
                    claims.getSubject(),
                    claims.get("email", String.class),
                    Set.of(claims.get("roles", String.class).split(",")),
                    claims.getExpiration().getTime()
            );
        } catch (Exception ex) {
            throw new JwtValidationException("JWT parsing failed", ex);
        }
    }


}
