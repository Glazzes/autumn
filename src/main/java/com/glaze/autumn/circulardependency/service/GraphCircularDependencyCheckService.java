package com.glaze.autumn.circulardependency.service;

import com.glaze.autumn.circulardependency.exception.CircularDependencyCheckException;
import com.glaze.autumn.circulardependency.exception.CircularDependencyExceptionHandler;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.instantiator.exception.ComponentNotFoundException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GraphCircularDependencyCheckService implements CircularDependencyCheckService {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final CircularDependencyExceptionHandler exceptionHandler = new CircularDependencyExceptionHandler();

    private final Class<?> startUpClass;
    private final Set<ClassModel> classModels;
    private final Map<Class<?>, Set<Class<?>>> graph;

    public GraphCircularDependencyCheckService(
            Set<ClassModel> classModels,
            Class<?> startUpClass
    ){
        this.classModels = classModels;
        this.startUpClass = startUpClass;
        this.graph = this.buildDependencyGraph();
    }

    @Override
    public void checkProjectDependencies() {
        logger.log(Level.INFO, "Look for circular dependencies in your project... \uD83D\uDCA4");

        Stack<Class<?>> currentBranch = new Stack<>();

        if(!this.graph.isEmpty()) {
            this.checkCircularDependenciesRecursively(startUpClass, currentBranch);
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

    private void checkCircularDependenciesRecursively(Class<?> currentNode, Stack<Class<?>> currentBranch){
        if(currentBranch.contains(currentNode)) {
            if(currentNode == currentBranch.peek()) {
                String errorMessage = currentNode.getName() + " requires a bean of itself that can not be injected";
                throw new CircularDependencyCheckException(errorMessage);
            }

            String errorMessage = exceptionHandler.getErrorMessage(currentNode, currentBranch);
            throw new CircularDependencyCheckException(errorMessage);
        }

        currentBranch.add(currentNode);

        Set<Class<?>> currentNodeDependencies = this.graph.get(currentNode);
        if(currentNode.isInterface()) {
            currentNodeDependencies = this.getDependenciesForInterface(currentNode);
        }

        if(currentNodeDependencies == null) {
            Class<?> previousNode = currentBranch.peek();
            String errorMessage = String.format(
                    "%s requires a bean of type %s that could not be found",
                    previousNode.getName(),
                    currentNode.getName());

            throw new ComponentNotFoundException(errorMessage);
        }

        for(Class<?> dependency : currentNodeDependencies) {
            this.checkCircularDependenciesRecursively(dependency, currentBranch);
        }

        currentBranch.pop();
    }

    private Set<Class<?>> getDependenciesForInterface(Class<?> clazz) {
        Set<Class<?>> dependencies = null;
        for (Class<?> key : this.graph.keySet()) {
            if (clazz.isAssignableFrom(key)) {
                dependencies = this.graph.get(key);
            }
        }

        return dependencies;
    }
}
