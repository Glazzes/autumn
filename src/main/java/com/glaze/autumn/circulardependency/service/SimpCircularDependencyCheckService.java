package com.glaze.autumn.circulardependency.service;

import com.glaze.autumn.circulardependency.exception.CircularDependencyCheckException;
import com.glaze.autumn.circulardependency.exception.CircularDependencyExceptionHandler;
import com.glaze.autumn.clscanner.model.ClassModel;

import java.lang.reflect.Field;
import java.util.*;

public class SimpCircularDependencyCheckService implements CircularDependencyCheckService {
    private final CircularDependencyExceptionHandler exceptionHandler = new CircularDependencyExceptionHandler();

    private final Class<?> startUpClass;
    private final Set<ClassModel> classModels;

    public SimpCircularDependencyCheckService(
            Set<ClassModel> classModels,
            Class<?> startUpClass
    ){
        this.classModels = classModels;
        this.startUpClass = startUpClass;
    }

    @Override
    public void checkProjectDependencies() {
        Stack<Class<?>> currentBranch = new Stack<>();
        Map<Class<?>, Set<Class<?>>> projectDependencyGraph = this.buildDependencyGraph();
        checkCircularDependenciesRecursively(projectDependencyGraph, startUpClass, currentBranch);
    }

    private Map<Class<?>, Set<Class<?>>> buildDependencyGraph() {
        Map<Class<?>, Set<Class<?>>> dependencyGraph = new HashMap<>();
        for(ClassModel classModel : classModels) {
            Set<Class<?>> dependencies = new HashSet<>();
            for(Field field : classModel.getAutowiredFields()) {
                dependencies.add(field.getType());
            }

            dependencies.addAll(Arrays.asList(classModel.getConstructor().getParameterTypes()));
            dependencyGraph.put(classModel.getType(), dependencies);
        }

        return dependencyGraph;
    }

    private void checkCircularDependenciesRecursively(Map<Class<?>, Set<Class<?>>> graph, Class<?> currentNode, Stack<Class<?>> currentBranch){
        if(currentBranch.contains(currentNode)) {
            if(currentNode == currentBranch.peek()) {
                String errorMessage = currentNode.getName() + " requires a bean of itself that can not be injected";
                throw new CircularDependencyCheckException(errorMessage);
            }

            exceptionHandler.handleCircularDependencyConflict(currentNode, currentBranch);
        }

        currentBranch.add(currentNode);

        Set<Class<?>> currentNodeDependencies = graph.get(currentNode);
        for(Class<?> dependency : currentNodeDependencies) {
            this.checkCircularDependenciesRecursively(graph, dependency, currentBranch);
        }

        currentBranch.pop();
    }
}
