package com.glaze.autumn.configuration.annotation;

import com.glaze.autumn.annotations.*;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BasicContainerConfiguration implements AnnotationConfiguration {
    private final Set<Class<? extends Annotation>> componentAnnotations = new HashSet<>();
    private final Set<Class<? extends Annotation>> beanAnnotations = new HashSet<>();

    public BasicContainerConfiguration(){
        componentAnnotations.add(Component.class);
        componentAnnotations.add(Service.class);
        componentAnnotations.add(Repository.class);
        beanAnnotations.add(Bean.class);
    }

    @Override
    public void addComponentAnnotations(Collection<Class<? extends Annotation>> annotations) {
        this.componentAnnotations.addAll(annotations);
    }

    @Override
    public void addBeanAnnotations(Collection<Class<? extends Annotation>> annotations) {
        this.componentAnnotations.addAll(annotations);
    }

    @Override
    public Set<Class<? extends Annotation>> getComponentAnnotations() {
        return componentAnnotations;
    }

    @Override
    public Set<Class<? extends Annotation>> getBeanAnnotations() {
        return beanAnnotations;
    }
}
