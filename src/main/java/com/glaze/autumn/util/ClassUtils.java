package com.glaze.autumn.util;

import com.glaze.autumn.annotations.ComponentScan;
import com.glaze.autumn.application.exception.AutumnApplicationException;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ClassUtils {

    public static Collection<Class<?>> loadClasses(Collection<String> classes) {
        return classes.stream()
                .map(it -> {
                    try{
                        return Class.forName(it);
                    }catch (ClassNotFoundException e) {
                        throw new AutumnApplicationException("Could not load class" + it);
                    }
                })
                .collect(Collectors.toCollection(HashSet::new));
    }

    public static String[] getBasePackages(Class<?> clazz) {
        String[] basePackages = null;
        if(clazz.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = clazz.getAnnotation(ComponentScan.class);
            basePackages = componentScan.basePackages();
        }

        if(basePackages != null && basePackages.length == 0) return null;
        return basePackages;
    }

}
