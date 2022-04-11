package com.glaze.autumn.exceptions;

public class CircularDependencyCheckException extends RuntimeException {
    public CircularDependencyCheckException(String message) {
        super(message);
    }

    public CircularDependencyCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
