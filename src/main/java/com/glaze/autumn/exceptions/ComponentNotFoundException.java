package com.glaze.autumn.exceptions;

public final class ComponentNotFoundException extends RuntimeException{
    public ComponentNotFoundException(String message) {
        super(message);
    }
}
