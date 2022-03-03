package com.glaze.autumn.dependencyresolver.service;

import com.glaze.autumn.instantiator.exception.ComponentNotFoundException;
import com.glaze.autumn.instantiator.model.InstantiationModel;

import java.util.Collection;

public interface MissingDependencyHandler {
    void scanForMissingConstructorDependencies(Collection<InstantiationModel> models) throws ComponentNotFoundException;
    void scanForMissingFieldDependencies(Collection<InstantiationModel> models) throws ComponentNotFoundException;
}
