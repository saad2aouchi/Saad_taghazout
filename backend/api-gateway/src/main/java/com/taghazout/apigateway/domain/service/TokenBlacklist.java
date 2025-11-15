package com.taghazout.apigateway.domain.service;


/**
 * ISP: Separate concern for revocation.
 */
public interface TokenBlacklist {

    boolean isBlacklisted(String token);
}
