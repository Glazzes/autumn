package com.glaze.autumn.enums;

public enum FileExtension {
    CLASS(".class"),
    JAR(".jar");

    private final String value;
    FileExtension(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}
