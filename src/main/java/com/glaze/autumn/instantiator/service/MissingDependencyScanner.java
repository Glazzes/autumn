package com.glaze.autumn.instantiator.service;

public interface MissingDependencyScanner {
    void scanMissingAutowiredDependencies();
    void scanMissingConstructorDependencies();
}
