package com.glaze.autumn.dependencyresolver.service;

import com.glaze.autumn.annotations.Qualifier;
import com.glaze.autumn.instantiator.model.InstantiationModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class SimpDependencyResolverService implements DependencyResolverService {

    @Override
    public void resolveAutowiredFieldDependencies(InstantiationModel model, Map<String, Object> availableInstances) {
        Field[] autowiredFields = model.getAutowiredFields();
        Object[] autowiredFieldInstances = model.getAutowiredFieldDependencyInstances();

        for(int i = 0; i < autowiredFields.length; i++) {
            Field currentField = autowiredFields[i];
            Class<?> currentFieldType = currentField.getType();
            Object currentInstance = autowiredFieldInstances[i];

            if(currentInstance != null) continue;

            if(currentField.isAnnotationPresent(Qualifier.class)){
                Qualifier qualifier = currentField.getAnnotation(Qualifier.class);
                Object instance = availableInstances.get(qualifier.id());
                if(instance == null) continue;

                model.getAutowiredFieldDependencyInstances()[i] = instance;
                System.out.println(model.getAutowiredFieldDependencyInstances()[i]);
                continue;
            }

            for (Object instance : availableInstances.values()) {
                if(currentFieldType.isAssignableFrom(instance.getClass())){
                    model.getAutowiredFieldDependencyInstances()[i] = instance;
                }
            }
        }
    }

    @Override
    public void resolveConstructorDependencies(InstantiationModel model, Map<String, Object> availableInstances) {
        Class<?>[] constructorParameterTypes = model.getConstructorParameterTypes();
        Object[] constructorInstances = model.getConstructorDependencyInstances();

        for(int i = 0; i < constructorParameterTypes.length; i++) {
            Class<?> currentDependencyType = constructorParameterTypes[i];
            Object currentConstructorInstance = constructorInstances[i];
            Annotation[] constructorAnnotations = model.getConstructorParameterAnnotations()[i];

            if(currentConstructorInstance != null) continue;

            // Attempt id based look up if possible
            String qualifierId = this.getQualifierAnnotationId(constructorAnnotations);
            if(qualifierId != null) {
                Object availableInstance = availableInstances.get(qualifierId);
                if(availableInstance == null) continue;

                model.getConstructorDependencyInstances()[i] = availableInstance;
                continue;
            }

            // Attempt brute force look up
            for (Object dependency : availableInstances.values()) {
                if(currentDependencyType.isAssignableFrom(dependency.getClass())){
                    model.getConstructorDependencyInstances()[i] = dependency;
                }
            }

        }
    }

    private String getQualifierAnnotationId(Annotation[] annotations) {
        String id = null;
        for(Annotation annotation : annotations) {
            if(Qualifier.class.isAssignableFrom(annotation.getClass())){
                id = ((Qualifier) annotation).id();
                break;
            }
        }

        return id;
    }
}
