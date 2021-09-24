package com.glaze.autumn.circulardependency.service;

import com.glaze.autumn.circulardependency.exception.CircularDependencyInjectionException;
import com.glaze.autumn.circulardependency.exception.ExceptionMessageFormatter;
import com.glaze.autumn.circulardependency.model.CircularDependencyModel;

import java.util.*;

public class SimpCircularDependencyDetectionService implements CircularDependencyDetectionService{
    private final Map<Class<?>, CircularDependencyModel> circularDependencyModels;
    private final ExceptionMessageFormatter exceptionFormatter = new ExceptionMessageFormatter();

    public SimpCircularDependencyDetectionService(Map<Class<?>, CircularDependencyModel> circularDependencyModels){
        this.circularDependencyModels = circularDependencyModels;
    }

    @Override
    public void scan(){
        for(Map.Entry<Class<?>, CircularDependencyModel> cyclicModelEntry : circularDependencyModels.entrySet()) {
            Class<?> rootClass = cyclicModelEntry.getValue()
                    .getClassModel()
                    .getType();

            LinkedList<Class<?>> nodes = new LinkedList<>();
            this.scanCircularDependenciesRecursively(rootClass, null, nodes);
        }
    }

    private void scanCircularDependenciesRecursively(final Class<?> rootClass, Class<?> currentClass, LinkedList<Class<?>> nodes){
        if(nodes.contains(rootClass)){
            this.onRootNodeCircularDependency(nodes.toArray(Class[]::new));
        }

        if(nodes.contains(currentClass)){
            if(nodes.indexOf(currentClass) != nodes.size() - 1){
                this.onSubNodeCircularDependency(rootClass, currentClass, nodes);
            }
        }

        Class<?>[] currentClassDependencies = currentClass == null
                ? this.getClassDependencies(rootClass)
                : this.getClassDependencies(currentClass);

        for(Class<?> dependency : currentClassDependencies){
            nodes.addLast(dependency);
            this.scanCircularDependenciesRecursively(rootClass, dependency, nodes);
        }

        if(nodes.size() > 0) nodes.removeLast();
    }

    private void onRootNodeCircularDependency(Class<?>[] circularDependencies) {
        String errorMessage = exceptionFormatter.getMessageOnRootNodeCircularDependency(circularDependencies);
        throw new CircularDependencyInjectionException(errorMessage);
    }

    private void onSubNodeCircularDependency(Class<?> rootClass, Class<?> causedBy, List<Class<?>> circularDependencies){
        Class<?>[] circularDependencyArr = circularDependencies.stream()
                .filter(Objects::nonNull)
                .toArray(Class[]::new);

        String errorMessage = exceptionFormatter.getMessageOnSubNodesCircularDependency(
                rootClass,
                causedBy,
                circularDependencyArr
        );

        System.out.println(Arrays.toString(circularDependencyArr));
        throw new CircularDependencyInjectionException(errorMessage);
    }

    private Class<?>[] getClassDependencies(Class<?> cls){
        return Optional.of(circularDependencyModels.get(cls))
                .map(model -> model.getClassModel()
                        .getConstructor()
                        .getParameterTypes()
                )
                .orElseThrow(() -> {
                    String errorMessage = String.format("""
                    A bean of type %s was required but none were present
                    """, cls.getName());

                    return new IllegalArgumentException(errorMessage);
                });
    }

}
