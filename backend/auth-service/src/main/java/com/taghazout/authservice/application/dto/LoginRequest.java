package com.taghazout.authservice.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for login data transfer
 * - Immutable: All fields are final
 * 
 * Validation Rules:
 * - Email: Must be valid format and not blank
 * - Password: Must not be blank (no min length for login)
 * 
 * Security Note:
 * - Password sent over HTTPS only
 * - Never logged or exposed in toString()
 */
public record LoginRequest(

        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

        @NotBlank(message = "Password is required") String password) {
    /**
     * Compact constructor for normalization.
     */
    public LoginRequest {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }

    /**
     * Override toString to hide password.
     */
    @Override
    public String toString() {
        return "LoginRequest{email='" + email + "', password='[PROTECTED]'}";
    }
}
