package com.glaze.autumn.exceptions;

public class ComponentInstantiationException extends RuntimeException{
    public ComponentInstantiationException(String message) {
        super(message);
    }

    public ComponentInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
