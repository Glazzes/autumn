package com.glaze.autumn.services.circulardependencyscanner;

import com.glaze.autumn.exceptions.CircularDependencyCheckException;
import com.glaze.autumn.formatters.CircularDependencyExceptionFormatter;
import com.glaze.autumn.models.ClassModel;
import com.glaze.autumn.exceptions.ComponentNotFoundException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GraphCircularDependencyCheckService implements CircularDependencyCheckService {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final CircularDependencyExceptionFormatter exceptionHandler = new CircularDependencyExceptionFormatter();
    private final Set<ClassModel> classModels;
    private final Collection<Class<?>> beans;
    private final Map<Class<?>, Set<Class<?>>> graph;

    public GraphCircularDependencyCheckService(Set<ClassModel> classModels){
        this.classModels = classModels;
        this.beans = classModels.stream()
                .flatMap(it -> Arrays.stream(it.getBeans()))
                .map(Method::getReturnType)
                .collect(Collectors.toCollection(HashSet::new));

        this.graph = this.buildDependencyGraph();
    }

    @Override
    public void scanProjectDependencies() {
        logger.log(Level.INFO, "Looking for circular dependencies in your project... \uD83D\uDCA4");

        if(!this.graph.isEmpty()) {
            for(Class<?> node : this.graph.keySet()) {
                this.lookForCircularDependencies(node, new Stack<>());
            }
        }

        logger.log(Level.INFO ,"No circular dependencies detected ✅");
    }

    private Map<Class<?>, Set<Class<?>>> buildDependencyGraph() {
        Map<Class<?>, Set<Class<?>>> dependencyGraph = new HashMap<>();
        for(ClassModel classModel : classModels) {
            Set<Class<?>> dependencies = new HashSet<>();
            Collection<Class<?>> fieldDependencies = Arrays.stream(classModel.getAutowiredFields())
                    .filter(Objects::nonNull)
                    .map(Field::getType)
                    .collect(Collectors.toCollection(HashSet::new));

            dependencies.addAll(fieldDependencies);
            dependencies.addAll(Arrays.asList(classModel.getConstructor().getParameterTypes()));
            dependencyGraph.put(classModel.getType(), dependencies);
        }

        return dependencyGraph;
    }

    private void lookForCircularDependencies(Class<?> currentNode, Stack<Class<?>> currentBranch){
        if(currentBranch.contains(currentNode)) {
            if(currentNode == currentBranch.peek()) {
                String errorMessage = currentNode.getName() + " requires a bean of itself that can not be injected";
                throw new CircularDependencyCheckException(errorMessage);
            }

            String errorMessage = exceptionHandler.getErrorMessage(currentNode, currentBranch);
            throw new CircularDependencyCheckException(errorMessage);
        }

        Set<Class<?>> currentNodeDependencies = this.getNodeDependencies(currentNode);
        if(currentNodeDependencies == null) {
            if(this.beans.contains(currentNode)) {
                return;
            }

            Class<?> previousNode = currentBranch.peek();
            String errorMessage = String.format(
                    "%s requires a bean of type %s that could not be found in your project",
                    previousNode,
                    currentNode.getName());

            throw new ComponentNotFoundException(errorMessage);
        }

        currentBranch.add(currentNode);
        for(Class<?> dependency : currentNodeDependencies) {
            this.lookForCircularDependencies(dependency, currentBranch);
        }

        currentBranch.pop();
    }

    private Set<Class<?>> getNodeDependencies(Class<?> currentNode) {
        if(currentNode.isInterface()) {
            for (Class<?> key : this.graph.keySet()) {
                if (currentNode.isAssignableFrom(key)) {
                    return this.graph.get(key);
                }
            }
        }

        return this.graph.get(currentNode);
    }
}
