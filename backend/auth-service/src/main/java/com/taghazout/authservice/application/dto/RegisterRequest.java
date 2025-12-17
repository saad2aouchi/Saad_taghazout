package com.taghazout.authservice.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration request.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for registration data transfer
 * - Immutable: All fields are final (defensive design)
 * 
 * Validation Rules:
 * - Email: Must be valid format and not blank
 * - Password: Minimum 8 characters (will be hashed with BCrypt)
 * - First/Last Name: Optional, max 100 characters
 * 
 * Note: This is a record (Java 16+) providing:
 * - Immutability
 * - Automatic getters
 * - toString(), equals(), hashCode()
 * - Compact constructor for validation
 */
public record RegisterRequest(

        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Size(max = 255, message = "Email must not exceed 255 characters") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") String password,

        @Size(max = 100, message = "First name must not exceed 100 characters") String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters") String lastName,

        String organizationName) {
    /**
     * Compact constructor for additional validation.
     * Executes before field initialization.
     */
    public RegisterRequest {
        // Trim whitespace from strings
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
    }

    /**
     * Simplified constructor for email and password only.
     */
    public RegisterRequest(String email, String password) {
        this(email, password, null, null, null);
    }
}
