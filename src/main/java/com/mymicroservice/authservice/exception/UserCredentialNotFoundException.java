package com.mymicroservice.authservice.exception;

import jakarta.persistence.EntityNotFoundException;

public class UserCredentialNotFoundException extends EntityNotFoundException {

    public UserCredentialNotFoundException(String message) {
        super(message);
    }
}
