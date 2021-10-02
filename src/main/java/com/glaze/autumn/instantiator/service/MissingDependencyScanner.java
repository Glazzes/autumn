package com.glaze.autumn.instantiator.service;

import com.glaze.autumn.instantiator.model.InstantiationQueuedModel;

public interface MissingDependencyScanner {
    void scanMissingAutowiredDependencies();
    void scanMissingConstructorDependencies();
}
