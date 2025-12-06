package com.taghazout.authservice.domain.exception;

/**
 * Exception thrown when attempting to register a user with an email that
 * already exists.
 * 
 * This is a business rule violation - emails must be unique across all users.
 * 
 * HTTP Status: 409 Conflict
 */
public class UserAlreadyExistsException extends RuntimeException {

    private final String email;

    public UserAlreadyExistsException(String email) {
        super("User with email already exists: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
