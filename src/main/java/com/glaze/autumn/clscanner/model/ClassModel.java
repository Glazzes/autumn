package com.glaze.autumn.clscanner.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ClassModel {
    private Class<?> type;
    private Field[] autowiredFields;
    private Constructor<?> constructor;
    private Method postConstruct;
    private Method[] beans;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Field[] getAutowiredFields() {
        return autowiredFields;
    }

    public void setAutowiredFields(Field[] autowiredFields) {
        this.autowiredFields = autowiredFields;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public Method getPostConstruct() {
        return postConstruct;
    }

    public void setPostConstruct(Method postConstruct) {
        this.postConstruct = postConstruct;
    }

    public Method[] getBeans() {
        return beans;
    }

    public void setBeans(Method[] beans) {
        this.beans = beans;
    }

    @Override
    public String toString() {
        return "ClassModel{" +
                ", type=" + type +
                ", autowiredFields=" + Arrays.toString(autowiredFields) +
                ", constructor=" + constructor +
                ", postConstruct=" + postConstruct +
                ", beans=" + Arrays.toString(beans) +
                '}';
    }
}
