package com.taghazout.apigateway.domain.model;

import java.util.Set;

public record UserPrincipal (
    String userId,
    String email,
    Set<String> roles,
    Long expirationTime
) {

        // add custom methods
        public boolean hasRole(String role) {
            return roles != null && roles.contains(role);
        }

    // Custom compact constructor for validation
    public UserPrincipal {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }
    }

}
