package com.glaze.autumn.shared.exception;

public class CyclicDependencyInjectionException extends RuntimeException {
    public CyclicDependencyInjectionException(String message) {
        super(message);
    }

    public CyclicDependencyInjectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
