package com.taghazout.apigateway.domain.exception;

public class JwtValidationException extends RuntimeException {

    public JwtValidationException(String message, Exception ex) {
        super(message);
    }

    // You might want to add:
    public JwtValidationException(String message) {
        super(message);
    }
}
