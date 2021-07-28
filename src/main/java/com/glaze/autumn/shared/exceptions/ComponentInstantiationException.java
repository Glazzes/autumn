package com.glaze.autumn.shared.exceptions;

public class ComponentInstantiationException extends RuntimeException{
    public ComponentInstantiationException(String message) {
        super(message);
    }

    public ComponentInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
