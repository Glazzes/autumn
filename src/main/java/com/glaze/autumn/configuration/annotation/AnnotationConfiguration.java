package com.glaze.autumn.configuration.annotation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

public interface AnnotationConfiguration {
    void addComponentAnnotations(Collection<Class<? extends Annotation>> annotations);
    void addBeanAnnotations(Collection<Class<? extends Annotation>> annotations);
    Set<Class<? extends Annotation>> getComponentAnnotations();
    Set<Class<? extends Annotation>> getBeanAnnotations();
}
