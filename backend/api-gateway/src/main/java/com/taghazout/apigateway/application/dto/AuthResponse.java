package com.taghazout.apigateway.application.dto;

//
//        * Immutable, localized, JSON-ready authentication response DTO


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class AuthResponse {
    @JsonProperty("message")
    private final String message;

    @JsonProperty("path")
    private final String path;

    @JsonProperty("status")
    private final int status;

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    // Private constructor with validation
    private AuthResponse(String message, String path, int status) {
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.path = Objects.requireNonNull(path, "Path cannot be null");

        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("HTTP status must be between 100-599");
        }
        this.status = status;

        this.timestamp = LocalDateTime.now();
    }

    // Complete set of factory methods
    public static AuthResponse success(String path) {
        return new AuthResponse("Success", path, 200);
    }

    public static AuthResponse created(String path) {
        return new AuthResponse("Created", path, 201);
    }

    public static AuthResponse badRequest(String path) {
        return new AuthResponse("Bad Request", path, 400);
    }

    public static AuthResponse unauthorized(String path) {
        return new AuthResponse("Unauthorized", path, 401);
    }

    public static AuthResponse forbidden(String path) {
        return new AuthResponse("Forbidden", path, 403);
    }

    public static AuthResponse notFound(String path) {
        return new AuthResponse("Not Found", path, 404);
    }

    public static AuthResponse tooManyRequests(String path) {
        return new AuthResponse("Too Many Requests", path, 429);
    }

    public static AuthResponse internalError(String path) {
        return new AuthResponse("Internal Server Error", path, 500);
    }

    // Flexible factory for custom scenarios
    public static AuthResponse of(String message, String path, int status) {
        return new AuthResponse(message, path, status);
    }

    // Getters
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public int getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Optional: toString, equals, hashCode for better logging
    @Override
    public String toString() {
        return String.format("AuthResponse{status=%d, path='%s', message='%s'}",
                status, path, message);
    }


    public byte[] toJsonBytes() {
        return String.format(
                "{\"message\":\"%s\",\"path\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}",
                message, path, status, timestamp
        ).getBytes();
    }
}
