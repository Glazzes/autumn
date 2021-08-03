package com.glaze.autumn.model;

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
    private Object clsModelTypeInstance;

    public InstantiationQueuedModel(ClassModel clsModel){
        this.classModel = clsModel;
        this.constructorDependencies = clsModel.getConstructor().getParameterTypes();
        this.constructorDependencyInstances = new Object[this.constructorDependencies.length];
        this.autowiredFields = clsModel.getAutowiredFields();
        this.autowiredFieldDependencyInstances = new Object[this.autowiredFields.length];
        this.beans = classModel.getBeans();
    }

    public boolean areConstructorDependenciesResolved(){
        if(this.constructorDependencyInstances.length == 0) return true;
        return Arrays.stream(this.constructorDependencyInstances)
                .noneMatch(Objects::isNull);
    }

    public boolean areAutowireFieldsDependenciesResolved(){
        if(this.autowiredFields == null) return true;
        if(this.autowiredFields.length == 0) return true;
        return Arrays.stream(this.autowiredFieldDependencyInstances)
                .noneMatch(Objects::isNull);
    }

    public boolean isModelResolved(){
        return this.areConstructorDependenciesResolved()
                && this.areAutowireFieldsDependenciesResolved();
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

    public Object getClsModelTypeInstance() {
        return clsModelTypeInstance;
    }

    public void setClsModelTypeInstance(Object clsModelTypeInstance) {
        this.clsModelTypeInstance = clsModelTypeInstance;
    }

    public Method[] getBeans() {
        return beans;
    }
}
