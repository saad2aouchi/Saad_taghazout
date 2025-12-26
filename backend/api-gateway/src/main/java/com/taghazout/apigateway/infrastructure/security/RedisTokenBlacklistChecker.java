package com.taghazout.apigateway.infrastructure.security;

import com.taghazout.apigateway.domain.service.TokenBlacklist;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * SRP: ONLY knows how to check/store revoked tokens.
 * No JWT, no crypto, no business logic.
 */
@Component
public class RedisTokenBlacklistChecker implements TokenBlacklist {

    private static final String PREFIX = "blacklist : ";
    private final RedisTemplate<String, String> redis;

    RedisTokenBlacklistChecker(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redis.hasKey(PREFIX + token));
        } catch (Exception e) {
            System.err.println("🔴 REDIS ERROR in TokenBlacklist: " + e.getMessage());
            e.printStackTrace();
            return true; // If we can't check, assume revoked
        }
    }

    void blacklist(String token, long ttlMillis) {
        redis.opsForValue().set(PREFIX + token, "1", ttlMillis, TimeUnit.MILLISECONDS);
    }

}
