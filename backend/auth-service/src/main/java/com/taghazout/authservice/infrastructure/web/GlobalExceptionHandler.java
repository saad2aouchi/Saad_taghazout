package com.taghazout.authservice.infrastructure.web;

import com.taghazout.authservice.domain.exception.InvalidCredentialsException;
import com.taghazout.authservice.domain.exception.InvalidRefreshTokenException;
import com.taghazout.authservice.domain.exception.UserAlreadyExistsException;
import com.taghazout.authservice.domain.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for exception-to-HTTP mapping
 * - OCP: Can be extended with more exception handlers
 * 
 * Provides consistent error responses across all endpoints:
 * - HTTP status codes
 * - Error messages
 * - Timestamps
 * - Validation errors
 * 
 * Error Response Format:
 * {
 * "timestamp": "2024-01-15T10:30:00",
 * "status": 409,
 * "error": "Conflict",
 * "message": "User with email already exists: test@example.com",
 * "path": "/api/v1/auth/register"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles UserAlreadyExistsException (409 Conflict).
     * 
     * Thrown during registration when email already exists.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.CONFLICT,
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles UserNotFoundException (404 Not Found).
     * 
     * Typically won't be exposed to client (security - don't reveal if user
     * exists).
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles InvalidCredentialsException (401 Unauthorized).
     * 
     * Thrown during login when credentials are invalid or account disabled/locked.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles InvalidRefreshTokenException (401 Unauthorized).
     * 
     * Thrown when refresh token is invalid, expired, or revoked.
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles validation errors (400 Bad Request).
     * 
     * Triggered by @Valid annotation on controller parameters.
     * Returns field-specific validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("timestamp", LocalDateTime.now().toString());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Validation Failed");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        errors.put("errors", fieldErrors);

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles IllegalArgumentException (400 Bad Request).
     * 
     * Thrown by domain entities for invalid input.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles all other exceptions (500 Internal Server Error).
     * 
     * Fallback for unexpected errors.
     * Logs full stack trace but returns generic message to client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log the full exception (would use logger in production)
        ex.printStackTrace();

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Standard error response DTO.
     */
    public record ErrorResponse(
            String timestamp,
            int status,
            String error,
            String message) {
        public static ErrorResponse of(HttpStatus status, String message) {
            return new ErrorResponse(
                    LocalDateTime.now().toString(),
                    status.value(),
                    status.getReasonPhrase(),
                    message);
        }
    }
}
