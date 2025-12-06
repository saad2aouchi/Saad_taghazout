package com.taghazout.authservice.domain.exception;

/**
 * Exception thrown when user is not found in the repository.
 * 
 * Typically occurs during:
 * - Login with non-existent email
 * - User lookup operations
 * - Profile updates for deleted users
 * 
 * HTTP Status: 404 Not Found
 */
public class UserNotFoundException extends RuntimeException {

    private final String identifier;

    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier);
        this.identifier = identifier;
    }

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
        this.identifier = String.valueOf(userId);
    }

    public String getIdentifier() {
        return identifier;
    }
}
