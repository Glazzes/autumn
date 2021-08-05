package com.glaze.autumn.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CircularDependencyModel {
    private final ClassModel classModel;
    private final Field[] autowiredFields;
    private final Class<?>[] constructorDependencyTypes;

    public CircularDependencyModel(ClassModel model){
        this.classModel = model;
        this.constructorDependencyTypes = model.getConstructor().getParameterTypes();
        this.autowiredFields = model.getAutowiredFields();
    }

    public Class<?>[] getAllRequiredDependencies(){
        List<Class<?>> autowiredDependencies = this.autowiredFields != null ?
                Arrays.stream(this.autowiredFields)
                        .map(Field::getType)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        List<Class<?>> constructorDependencies = this.constructorDependencyTypes.length > 0 ?
                 Arrays.stream(this.constructorDependencyTypes)
                    .collect(Collectors.toList())
                : new ArrayList<>();

        constructorDependencies.addAll(autowiredDependencies);

        return constructorDependencies.toArray(Class<?>[]::new);
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    public Field[] getAutowiredFields() {
        return autowiredFields;
    }

    public Class<?>[] getConstructorDependencyTypes() {
        return constructorDependencyTypes;
    }
}
