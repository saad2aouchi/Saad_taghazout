package com.taghazout.apigateway.infrastructure.security;

import com.taghazout.apigateway.domain.exception.JwtValidationException;
import com.taghazout.apigateway.domain.model.UserPrincipal;
import com.taghazout.apigateway.domain.service.JwtParser;
import com.taghazout.apigateway.domain.service.JwtValidator;
import com.taghazout.apigateway.domain.service.TokenBlacklist;
import org.springframework.stereotype.Component;


/**
 * SRP: ONLY orchestrates parsing + revocation check.
 * No crypto, no Redis, no secret management.
 * Open for extension (new rules via new decorators).
 */
@Component
public class DefaultJwtValidator implements JwtValidator{

    private final JwtParser parser;
    private final TokenBlacklist revocationChecker;

    DefaultJwtValidator(JwtParser parser, TokenBlacklist revocationChecker) {
        this.parser = parser;
        this.revocationChecker = revocationChecker;
    }

    @Override
    public UserPrincipal validateToken(String token) {
        // 1. Check revocation FIRST (cheap operation)
        if (revocationChecker.isBlacklisted(token)) {
            throw new JwtValidationException("Token revoked");
        }

        // 2. THEN parse (expensive crypto operation)
        return parser.parse(token);
    }

}
