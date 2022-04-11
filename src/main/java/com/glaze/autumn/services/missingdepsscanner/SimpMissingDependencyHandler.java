package com.glaze.autumn.services.missingdepsscanner;

import com.glaze.autumn.annotations.Qualifier;
import com.glaze.autumn.exceptions.ComponentNotFoundException;
import com.glaze.autumn.models.InstantiationModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

public class SimpMissingDependencyHandler implements MissingDependencyHandler {

    @Override
    public void scanForMissingConstructorDependencies(Collection<InstantiationModel> models) throws ComponentNotFoundException {
        for(InstantiationModel model : models) {
            Class<?>[] constructorDependencies = model.getConstructorParameterTypes();
            for (int dep = 0; dep < constructorDependencies.length; dep++) {
                Object dependency = model.getConstructorDependencyInstances()[dep];
                Annotation[] dependencyAnnotations = model.getConstructorParameterAnnotations()[dep];
                String id = this.getQualifierAnnotationId(dependencyAnnotations);

                if(id != null && dependency == null) {
                    String errorMessage = String.format("""
                    Constructor of %s required a bean of type %s with id "%s" at parameter %d, consider defining one         
                    """,
                    model.getType(),
                    model.getConstructorParameterTypes()[dep],
                    id,
                    dep);

                    throw new ComponentNotFoundException(errorMessage);
                }

                if (dependency == null) {
                    String errorMessage = String.format(
                            "Constructor of %s's required a bean of type %s, that could not be found, consider declaring one.",
                            model.getType(),
                            model.getConstructorParameterTypes()[dep]
                    );

                    throw new ComponentNotFoundException(errorMessage);
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

    @Override
    public void scanForMissingFieldDependencies(Collection<InstantiationModel> models) throws ComponentNotFoundException {
        for(InstantiationModel model : models){
            if(model.getAutowiredFields() == null) continue;

            Field[] autowiredFields = model.getAutowiredFields();
            for(int field = 0; field < autowiredFields.length; field++){
                Field currentField = autowiredFields[field];
                Object currentFieldInstance = model.getAutowiredFieldDependencyInstances()[field];

                if(currentFieldInstance == null){
                    if(currentField.isAnnotationPresent(Qualifier.class)){
                        Qualifier qualifier = autowiredFields[field].getAnnotation(Qualifier.class);
                        String errorMessage = String.format("""
                                %s required a bean at field named "%s" of type %s with id "%s" that could not be found, consider declaring one
                                """,
                                model.getType(),
                                currentField.getName(),
                                currentField.getType(),
                                qualifier.id()
                        );

                        throw new ComponentNotFoundException(errorMessage);
                    }

                    String errorMessage = String.format(
                            "%s required a bean of type %s at field  \"%s\" that could not be found, consider declaring one",
                            model.getType(),
                            currentField.getType(),
                            currentField.getName());

                    throw new ComponentNotFoundException(errorMessage);
                }
            }
        }
    }

}
