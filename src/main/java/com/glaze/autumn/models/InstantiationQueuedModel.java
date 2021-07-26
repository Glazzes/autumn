package com.glaze.autumn.models;

public class InstantiationQueuedModel {
    private final Class<?> classType;
    private final Class<?>[] dependencyTypes;
    private final Object[] dependencyInstances;

    public InstantiationQueuedModel(ClassModel clsModel){
        this.classType = clsModel.getType();
        this.dependencyTypes = clsModel.getConstructor().getParameterTypes();
        this.dependencyInstances = new Object[this.dependencyTypes.length];
    }

    public Class<?> getClassType() {
        return classType;
    }

    public Class<?>[] getDependencyTypes() {
        return dependencyTypes;
    }

    public Object[] getDependencyInstances() {
        return dependencyInstances;
    }
}
