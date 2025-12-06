package com.taghazout.authservice.domain.exception;

/**
 * Exception thrown when authentication credentials are invalid.
 * 
 * Reasons include:
 * - Wrong password
 * - Account disabled
 * - Account locked
 * 
 * Security Note: Don't reveal specific reason to prevent account enumeration
 * 
 * HTTP Status: 401 Unauthorized
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
