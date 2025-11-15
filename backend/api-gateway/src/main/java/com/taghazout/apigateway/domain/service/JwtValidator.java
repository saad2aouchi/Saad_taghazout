package com.taghazout.apigateway.domain.service;

import com.taghazout.apigateway.domain.model.UserPrincipal;

/**
 * ISP: High-level facade that orchestrates parsing + revocation.
 * Clients depend only on this, not on the low-level details.
 */
public interface JwtValidator {

    /**
     * Validates JWT token and extracts user principal
     * @param token JWT token
     * @return UserPrincipal if valid
//     * @throws JwtValidationException if invalid
     */
    UserPrincipal validateToken(String token);

}
