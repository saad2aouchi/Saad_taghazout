package com.taghazout.apigateway.domain.service;

import com.taghazout.apigateway.domain.model.UserPrincipal;

/**
 * ISP: Minimal contract for cryptographic validation only.
 */
public interface JwtParser {
    UserPrincipal parse(String token);
}