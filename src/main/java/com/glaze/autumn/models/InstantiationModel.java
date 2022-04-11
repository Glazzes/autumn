package com.glaze.autumn.models;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class InstantiationModel {
    private final Class<?> type;
    private final Constructor<?> constructor;
    private final Annotation[][] constructorParameterAnnotations;
    private final Class<?>[] constructorParameterTypes;
    private final Object[] constructorDependencyInstances;
    private final Field[] autowiredFields;
    private final Object[] autowiredFieldDependencyInstances;
    private final Method[] beans;
    private final Method postConstruct;
    private Object instance;

    public InstantiationModel(ClassModel classModel){
        this.type = classModel.getType();
        this.constructor = classModel.getConstructor();
        this.constructorParameterAnnotations = classModel.getConstructor().getParameterAnnotations();
        this.constructorParameterTypes = classModel.getConstructor().getParameterTypes();
        this.constructorDependencyInstances = new Object[this.constructorParameterTypes.length];
        this.autowiredFields = classModel.getAutowiredFields();
        this.autowiredFieldDependencyInstances = new Object[autowiredFields == null ? 0 : autowiredFields.length];
        this.beans = classModel.getBeans();
        this.postConstruct = classModel.getPostConstruct();
        this.instance = null;
    }

    public boolean isModelResolved(){
        return this.instance != null
                && this.hasConstructorDependenciesResolved()
                && this.hasAutowiredFieldsResolved();
    }

    public boolean hasConstructorDependenciesResolved(){
        if(this.constructorDependencyInstances.length == 0) return true;
        return Arrays.stream(this.constructorDependencyInstances)
                .noneMatch(Objects::isNull);
    }

    public boolean hasAutowiredFieldsResolved(){
        if(this.autowiredFieldDependencyInstances.length == 0) return true;
        return Arrays.stream(this.autowiredFieldDependencyInstances)
                .noneMatch(Objects::isNull);
    }

    public Annotation[][] getConstructorParameterAnnotations() {
        return constructorParameterAnnotations;
    }

    public Class<?>[] getConstructorParameterTypes() {
        return constructorParameterTypes;
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

    public Class<?> getType() {
        return type;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public Method getPostConstruct() {
        return postConstruct;
    }
}
