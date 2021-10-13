package com.glaze.autumn.circulardependency.service;

import com.glaze.autumn.circulardependency.exception.CircularDependencyInjectionException;
import com.glaze.autumn.circulardependency.exception.ExceptionMessageFormatter;
import com.glaze.autumn.circulardependency.model.CircularDependencyModel;
import com.glaze.autumn.application.exception.AutumnApplicationException;

import java.util.*;

public class SimpCircularDependencyDetectionService implements CircularDependencyDetectionService{
    private final Class<?> startUpClass;
    private final Map<Class<?>, CircularDependencyModel> circularDependencyModels;
    private final ExceptionMessageFormatter exceptionFormatter = new ExceptionMessageFormatter();

    public SimpCircularDependencyDetectionService(
            Map<Class<?>, CircularDependencyModel> circularDependencyModels,
            Class<?> startUpClass
    ){
        this.circularDependencyModels = circularDependencyModels;
        this.startUpClass = startUpClass;
    }

    @Override
    public void scan(){
        for(Map.Entry<Class<?>, CircularDependencyModel> cyclicModelEntry : circularDependencyModels.entrySet()) {
            Class<?> rootClass = cyclicModelEntry.getValue()
                    .getClassModel()
                    .getType();

            Stack<Class<?>> nodes = new Stack<>();
            this.scanCircularDependenciesRecursively(rootClass, null, nodes);
        }
    }

    private void scanCircularDependenciesRecursively(final Class<?> rootClass, Class<?> currentClass, Stack<Class<?>> nodes){
        if(nodes.contains(startUpClass)){
            this.onStartUpClassCircularDependency(nodes.get(nodes.size()-1));
        }

        if(nodes.contains(rootClass)){
            this.onRootNodeCircularDependency(nodes.toArray(Class[]::new));
        }

        if(rootClass == currentClass){
            String errorMessage = rootClass + " has a dependency on itself that can not be satisfied, consider refactoring";
            throw new CircularDependencyInjectionException(errorMessage);
        }

        if(nodes.contains(currentClass)){
            if(nodes.indexOf(currentClass) != nodes.size() - 1){
                this.onSubNodeCircularDependency(rootClass, currentClass, nodes);
            }
        }

        Class<?>[] currentClassDependencies = currentClass == null
                ? circularDependencyModels.get(rootClass).getAllRequiredDependencies()
                : circularDependencyModels.get(currentClass).getAllRequiredDependencies();

        for(Class<?> dependency : currentClassDependencies){
            nodes.push(dependency);
            this.scanCircularDependenciesRecursively(rootClass, dependency, nodes);
        }

        if(nodes.size() > 0) nodes.pop();
    }

    private void onStartUpClassCircularDependency(Class<?> causedBy){
        String errorMessage = String.format("""
        %s required a bean of the startup class that will no be satisfied, consider refactoring
        """, causedBy);

        throw new AutumnApplicationException(errorMessage);
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

        throw new CircularDependencyInjectionException(errorMessage);
    }

}
