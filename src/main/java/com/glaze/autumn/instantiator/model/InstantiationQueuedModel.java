package com.glaze.autumn.instantiator.model;

import com.glaze.autumn.clscanner.model.ClassModel;

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

    public boolean hasConstructorDependenciesResolved(){
        if(this.constructorDependencyInstances.length == 0) return true;
        return Arrays.stream(this.constructorDependencyInstances)
                .noneMatch(Objects::isNull);
    }

    public boolean hasAutowiredFieldsResolved(){
        if(this.autowiredFields == null) return true;
        if(this.autowiredFields.length == 0) return true;
        return Arrays.stream(this.autowiredFieldDependencyInstances)
                .noneMatch(Objects::isNull);
    }

    public boolean isModelResolved(){
        return this.hasConstructorDependenciesResolved()
                && this.hasAutowiredFieldsResolved();
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
