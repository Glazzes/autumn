package com.glaze.autumn.instantiator.service;

import com.glaze.autumn.clscanner.model.ClassModel;

import java.util.Map;
import java.util.Set;

public final class MainClassInstantiatorService extends SimpClassInstantiationService{
    private final Map<String, Object> availableInstances;

    public MainClassInstantiatorService(
            ClassModel mainClassModel,
            Map<String, Object> availableInstances
    ){
        super(Set.of(mainClassModel));
        this.availableInstances = availableInstances;
    }

    @Override
    public void instantiateComponents() {
        //this.resolveConstructorDependencies();
    }
}
