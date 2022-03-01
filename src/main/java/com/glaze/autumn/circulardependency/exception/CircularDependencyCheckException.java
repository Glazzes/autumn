package com.glaze.autumn.circulardependency.exception;

public class CircularDependencyCheckException extends RuntimeException {
    public CircularDependencyCheckException(String message) {
        super(message);
    }

    public CircularDependencyCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
