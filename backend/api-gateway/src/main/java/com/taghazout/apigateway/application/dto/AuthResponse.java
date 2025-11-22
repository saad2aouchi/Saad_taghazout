package com.taghazout.apigateway.application.dto;

//
//        * Immutable, localized, JSON-ready authentication response DTO


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class AuthResponse {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(); // Reusable, thread-safe

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
        } catch (IOException | JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize AuthResponse", e);
        }
    }
}
