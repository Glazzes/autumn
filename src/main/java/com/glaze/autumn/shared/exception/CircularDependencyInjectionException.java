package com.glaze.autumn.shared.exception;

public class CircularDependencyInjectionException extends RuntimeException {
    public CircularDependencyInjectionException(String message) {
        super(message);
    }

    public CircularDependencyInjectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
