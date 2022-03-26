package com.glaze.autumn.annotations.configuration;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

public interface AnnotationConfiguration {
    Collection<Class<? extends Annotation>> getAnnotations();
    void addAnnotation(Class<? extends Annotation> annotation);
}
