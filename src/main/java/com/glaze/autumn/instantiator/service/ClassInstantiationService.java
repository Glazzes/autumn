package com.glaze.autumn.instantiator.service;

import com.glaze.autumn.instantiator.model.InstantiationModel;

import java.util.Collection;
import java.util.Map;

public interface ClassInstantiationService {
    void instantiate();
    Collection<Object> getInstances();
}
