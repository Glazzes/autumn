package com.glaze.autumn.models;

import java.util.Arrays;
import java.util.Objects;

public class InstantiationQueuedModel {
    private final ClassModel classModel;
    private final Class<?>[] dependencyTypes;
    private final Object[] dependencyInstances;

    public InstantiationQueuedModel(ClassModel clsModel){
        this.classModel = clsModel;
        this.dependencyTypes = clsModel.getConstructor().getParameterTypes();
        this.dependencyInstances = new Object[this.dependencyTypes.length];
    }

    public boolean isResolved(){
        return Arrays.stream(this.dependencyInstances)
                .noneMatch(Objects::isNull);
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    public Class<?>[] getDependencyTypes() {
        return dependencyTypes;
    }

    public Object[] getDependencyInstances() {
        return dependencyInstances;
    }
}
