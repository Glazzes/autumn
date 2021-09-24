package com.glaze.autumn.circulardependency.exception;

public class CircularDependencyInjectionException extends RuntimeException {
    public CircularDependencyInjectionException(String message) {
        super(message);
    }

    public CircularDependencyInjectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
