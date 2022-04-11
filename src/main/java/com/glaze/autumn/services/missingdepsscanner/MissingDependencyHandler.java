package com.glaze.autumn.services.missingdepsscanner;

import com.glaze.autumn.exceptions.ComponentNotFoundException;
import com.glaze.autumn.models.InstantiationModel;

import java.util.Collection;

public interface MissingDependencyHandler {
    void scanForMissingConstructorDependencies(Collection<InstantiationModel> models) throws ComponentNotFoundException;
    void scanForMissingFieldDependencies(Collection<InstantiationModel> models) throws ComponentNotFoundException;
}
