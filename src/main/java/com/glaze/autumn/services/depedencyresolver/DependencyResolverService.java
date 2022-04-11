package com.glaze.autumn.services.depedencyresolver;

import com.glaze.autumn.models.InstantiationModel;

import java.util.Map;

public interface DependencyResolverService {
    void resolveAutowiredFieldDependencies(InstantiationModel model, Map<String, Object> availableInstances);
    void resolveConstructorDependencies(InstantiationModel model, Map<String, Object> availableInstances);
}
