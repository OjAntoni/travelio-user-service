package com.example.userservice.exception;

public class EntityNotFoundException extends NullPointerException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
