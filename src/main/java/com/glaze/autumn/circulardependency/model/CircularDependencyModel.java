package com.glaze.autumn.circulardependency.model;

import com.glaze.autumn.clscanner.model.ClassModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CircularDependencyModel {
    private final Class<?> type;
    private final ClassModel classModel;
    private final Field[] autowiredFields;
    private final Class<?>[] constructorDependencyTypes;

    public CircularDependencyModel(ClassModel model){
        this.type = model.getType();
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

        List<Class<?>> constructorDependencies = this.constructorDependencyTypes.length > 0
                ? Arrays.stream(this.constructorDependencyTypes)
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return Stream.of(autowiredDependencies, constructorDependencies)
                .flatMap(List::stream)
                .toArray(Class[]::new);
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    public Class<?> getType() {
        return type;
    }
}
