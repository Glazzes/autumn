package com.glaze.autumn.annotations.configuration;

import com.glaze.autumn.annotations.*;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;

public class Annotations implements AnnotationConfiguration {
    private final Collection<Class<? extends Annotation>> annotations = new HashSet<>();

    public Annotations() {
        annotations.add(Component.class);
        annotations.add(Repository.class);
        annotations.add(Service.class);
        annotations.add(Configuration.class);
        annotations.add(AutumnApplication.class);
    }

    @Override
    public Collection<Class<? extends Annotation>> getAnnotations() {
        return this.annotations;
    }

    @Override
    public void addAnnotation(Class<? extends Annotation> annotation) {
        this.annotations.add(annotation);
    }
}
