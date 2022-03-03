package com.glaze.autumn.dependencyresolver.service;

import com.glaze.autumn.annotations.Qualifier;
import com.glaze.autumn.instantiator.exception.ComponentNotFoundException;
import com.glaze.autumn.instantiator.model.InstantiationModel;

import java.lang.reflect.Field;
import java.util.Collection;

public class SimpMissingDependencyHandler implements MissingDependencyHandler {

    @Override
    public void scanForMissingConstructorDependencies(Collection<InstantiationModel> models) throws ComponentNotFoundException {
        for(InstantiationModel model : models) {
            Class<?>[] constructorDependencies = model.getConstructorParameterTypes();
            for (int dep = 0; dep < constructorDependencies.length; dep++) {
                Object dependency = model.getConstructorDependencyInstances()[dep];
                if (dependency == null) {
                    String errorMessage = String.format(
                            "%s's required a bean of type %s that could not be found, consider declaring one.",
                            model.getType(),
                            model.getConstructorParameterTypes()[dep]
                    );

                    throw new ComponentNotFoundException(errorMessage);
                }
            }
        }
    }

    @Override
    public void scanForMissingFieldDependencies(Collection<InstantiationModel> models) throws ComponentNotFoundException {
        for(InstantiationModel model : models){
            if(model.getAutowiredFields() == null) continue;

            Field[] autowiredFields = model.getAutowiredFields();
            for(int field = 0; field < autowiredFields.length; field++){
                if(model.getAutowiredFieldDependencyInstances()[field] == null){
                    if(autowiredFields[field].isAnnotationPresent(Qualifier.class)){
                        Qualifier qualifier = autowiredFields[field].getAnnotation(Qualifier.class);
                        String errorMessage = String.format(
                                "%s required a bean of type %s with id %s that could not be found, consider declaring one",
                                model.getType(),
                                autowiredFields[field].getType().getTypeName(),
                                qualifier.id()
                        );

                        throw new ComponentNotFoundException(errorMessage);
                    }

                    String errorMessage = String.format(
                            "%s required a bean of type %s that could not be found, consider declaring one",
                            model.getType(),
                            autowiredFields[field].getType().getTypeName()
                    );

                    throw new ComponentNotFoundException(errorMessage);
                }
            }
        }
    }

}
