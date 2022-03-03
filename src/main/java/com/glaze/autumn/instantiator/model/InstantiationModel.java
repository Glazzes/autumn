package com.glaze.autumn.instantiator.model;

import com.glaze.autumn.annotations.Qualifier;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.instantiator.exception.ComponentNotFoundException;

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
        return this.hasConstructorDependenciesResolved()
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

    public void onUnresolvedAutowiredFieldDependencies(){
        if(this.autowiredFields == null) return;

        for(int dep = 0; dep < autowiredFields.length; dep++){
            Field currentField = autowiredFields[dep];
            if(currentField.isAnnotationPresent(Qualifier.class) && autowiredFieldDependencyInstances[dep] == null){
                Qualifier qualifier = currentField.getAnnotation(Qualifier.class);
                String errorMessage = String.format("""
                %s required a bean of type %s with id %s that could not be found, consider declaring one.
                """, this.getType(), currentField.getType(), qualifier.id());

                throw new ComponentNotFoundException(errorMessage);
            }
        }
    }

    public void onUnresolvedConstructorDependencies(){
        for(int dep = 0; dep < constructorParameterTypes.length; dep++){
            Class<?> dependencyType = constructorParameterTypes[dep];
            Object instance = constructorDependencyInstances[dep];
            Annotation[] dependencyAnnotations = constructorParameterAnnotations[dep];

            if(dependencyAnnotations != null && dependencyAnnotations.length > 0){
                Qualifier qualifier = null;
                for(Annotation ann : dependencyAnnotations){
                    if(Qualifier.class.isAssignableFrom(ann.getClass())){
                        qualifier = (Qualifier) ann;
                        break;
                    }
                }

                if(qualifier != null && instance == null){
                    String errorMessage = String.format(
                            """
                            Constructor of %s required a bean of type %s with id "%s" that could not be found, consider
                            declaring one.
                            """,
                            this.getType(),
                            dependencyType,
                            qualifier.id()
                    );

                    throw new ComponentNotFoundException(errorMessage);
                }
            }

            if(instance == null){
                Class<?> type = this.getType();
                String errorMessage = String.format("""
                Constructor of %s required a bean of type %s that could not be found or instantiated properly
                """, type, constructorParameterTypes[dep]);

                throw new ComponentNotFoundException(errorMessage);
            }
        }
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
