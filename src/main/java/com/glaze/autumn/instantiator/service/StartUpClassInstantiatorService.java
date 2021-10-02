package com.glaze.autumn.instantiator.service;

import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.shared.exception.AutumnApplicationException;

import java.lang.reflect.Method;
import java.util.Map;

public class StartUpClassInstantiatorService implements ClassInstantiationService{
    private final ClassModel model;
    public StartUpClassInstantiatorService(ClassModel model) {
        this.model = model;
    }

    @Override
    public void instantiateComponents() {
        this.isStartUpClassValid(model.getType());
    }

    private void isStartUpClassValid(Class<?> startUpClass){
        if(!startUpClass.isAnnotationPresent(com.glaze.autumn.annotations.AutumnApplication.class)){
            String errorMessage = String.format("""
            Startup class %s must annotated with @AutumnApplication annotation""", startUpClass.getTypeName());

            throw new AutumnApplicationException(errorMessage);
        }
    }

    @Override
    public void instantiateBeans(Method[] beans, Object instance) {

    }

    @Override
    public Map<String, Object> getInstances() {
        return null;
    }
}
