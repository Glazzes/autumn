package com.glaze.autumn.instantiator.model;

import com.glaze.autumn.annotations.Qualifier;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.instantiator.exception.ComponentNotFoundException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class InstantiationQueuedModel {
    private final ClassModel classModel;
    private final Class<?>[] constructorDependencies;
    private final Object[] constructorDependencyInstances;
    private final Field[] autowiredFields;
    private final Object[] autowiredFieldDependencyInstances;
    private final Method[] beans;
    private Object instance;

    public InstantiationQueuedModel(ClassModel clsModel){
        int numberOfAutowiredFields = clsModel.getAutowiredFields() == null
                ? 0
                : clsModel.getAutowiredFields().length;

        this.classModel = clsModel;
        this.constructorDependencies = clsModel.getConstructor().getParameterTypes();
        this.constructorDependencyInstances = new Object[this.constructorDependencies.length];
        this.autowiredFields = clsModel.getAutowiredFields();
        this.autowiredFieldDependencyInstances = new Object[numberOfAutowiredFields];
        this.beans = classModel.getBeans();
    }

    public boolean isModelResolved(){
        return this.hasConstructorDependenciesResolved()
                && this.hasAutowiredFieldsResolved();
    }

    public boolean hasConstructorDependenciesResolved(){
        if(this.constructorDependencyInstances.length == 0) return true;
        return Arrays.stream(this.constructorDependencyInstances)
                .noneMatch(Objects::isNull);
    }

    public boolean hasAutowiredFieldsResolved(){
        if(this.autowiredFields == null) return true;
        if(this.autowiredFieldDependencyInstances.length == 0) return true;
        return Arrays.stream(this.autowiredFieldDependencyInstances)
                .noneMatch(Objects::isNull);
    }

    public void onUnresolvedAutowiredFieldDependencies(){
        if(this.autowiredFields == null) return;

        for(int dep = 0; dep < autowiredFields.length; dep++){
            Field currentField = autowiredFields[dep];
            if(currentField.isAnnotationPresent(Qualifier.class) && autowiredFieldDependencyInstances[dep] == null){
                Qualifier qualifier = currentField.getAnnotation(Qualifier.class);
                String errorMessage = String.format("""
                %s required a bean of type %s with id %s that could not be found, consider declaring one.
                """, classModel.getType(), currentField.getType(), qualifier.id());

                throw new ComponentNotFoundException(errorMessage);
            }
        }
    }

    public void onUnresolvedConstructorDependencies(){
        for(int dep = 0; dep < constructorDependencies.length; dep++){
            if(constructorDependencyInstances[dep] == null){
                Class<?> type = classModel.getType();
                String errorMessage = String.format("""
                Constructor of %s required a bean of type %s that could not be found or instantiated properly
                """, type, constructorDependencies[dep]);

                throw new ComponentNotFoundException(errorMessage);
            }
        }
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    public Class<?>[] getConstructorDependencies() {
        return constructorDependencies;
    }

    public Object[] getConstructorDependencyInstances() {
        return constructorDependencyInstances;
    }

    public Field[] getAutowiredFields() {
        return autowiredFields;
    }

    public Object[] getAutowiredFieldDependencyInstances() {
        return autowiredFieldDependencyInstances;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Method[] getBeans() {
        return beans;
    }
}
