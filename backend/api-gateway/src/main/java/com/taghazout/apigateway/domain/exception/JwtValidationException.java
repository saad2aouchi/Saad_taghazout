package com.taghazout.apigateway.domain.exception;

public class JwtValidationException extends RuntimeException {

    public JwtValidationException(String message, Exception ex) {
        super(message);
    }


    public JwtValidationException(String message) {
        super(message);
    }
}
