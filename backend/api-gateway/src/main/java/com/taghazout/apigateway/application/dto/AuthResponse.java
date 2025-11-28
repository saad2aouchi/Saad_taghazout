package com.taghazout.apigateway.application.dto;

//
//        * Immutable, localized, JSON-ready authentication response DTO



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.io.IOException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class AuthResponse {
    // ✅ ObjectMapper for Java 8 Date/Time
    private static final ObjectMapper OBJECT_MAPPER = Jackson2ObjectMapperBuilder.json()
            .modules(new JavaTimeModule())  // ← Adds LocalDateTime support
            .build();

    private final String message;
    private final String path;
    private final int status;
    private final LocalDateTime timestamp;

    private AuthResponse(String message, String path, int status) {
        this.message = message;
        this.path = path;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Factory method for creating auth responses.
     * Used by JwtAuthenticationFilter to build error responses dynamically.
     *

     * @param message Error description (e.g., "Unauthorized")
     * @param path Request path that triggered the error
     * @param status HTTP status code (e.g., 401, 403)
     * @return New immutable AuthResponse instance
     */
    
    public static AuthResponse unauthorized(String path) {
        return new AuthResponse("Unauthorized: Invalid or missing JWT token", path, 401);
    }

    public static AuthResponse forbidden(String path) {
        return new AuthResponse("Forbidden: Insufficient permissions", path, 403);
    }

    public static AuthResponse badRequest(String path, String details) {
        return new AuthResponse("Bad Request: " + details, path, 400);
    }
    
    
    public static AuthResponse of(String message, String path, int status) {
        return new AuthResponse(message, path, status);
    }

    // Getters only – immutable
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public int getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }

    /**
     * Serializes this DTO to JSON bytes for HTTP response body.
     *
     * @return byte array of JSON representation
     */
    public byte[] toJsonBytes() {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(this);
        } catch (JsonProcessingException e) {
            return fallbackJson(e.getMessage());
        }
    }

    private byte[] fallbackJson(String error) {
        String simpleJson = String.format(
                "{\"message\":\"%s\",\"path\":\"%s\",\"status\":%d,\"error\":\"Serialization failed: %s\"}",
                this.message, this.path, this.status, error
        );
        return simpleJson.getBytes();
    }
}
