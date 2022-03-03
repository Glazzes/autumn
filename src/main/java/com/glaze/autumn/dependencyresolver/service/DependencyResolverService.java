package com.glaze.autumn.dependencyresolver.service;

import com.glaze.autumn.instantiator.model.InstantiationModel;

import java.util.Map;

public interface DependencyResolverService {
    void resolveAutowiredFieldDependencies(InstantiationModel model, Map<String, Object> availableInstances);
    void resolveConstructorDependencies(InstantiationModel model, Map<String, Object> availableInstances);
}
