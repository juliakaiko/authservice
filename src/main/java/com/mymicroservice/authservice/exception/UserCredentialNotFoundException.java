package com.mymicroservice.authservice.exception;

public class UserCredentialNotFoundException extends RuntimeException {

    public UserCredentialNotFoundException(String message) {
        super(message);
    }
}
