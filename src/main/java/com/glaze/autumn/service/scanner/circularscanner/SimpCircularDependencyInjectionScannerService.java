package com.glaze.autumn.service.scanner.circularscanner;

import com.glaze.autumn.model.ClassModel;
import com.glaze.autumn.model.CircularDependencyModel;
import com.glaze.autumn.service.formatter.CyclicDependencyInjectionMessageFormatter;
import com.glaze.autumn.service.formatter.SimpCyclicDependencyInjectionMessageFormatter;
import com.glaze.autumn.shared.exception.CircularDependencyInjectionException;

import java.util.*;

public class SimpCircularDependencyInjectionScannerService implements CircularDependencyInjectionScannerService {
    private final Map<Class<?>, CircularDependencyModel> circularModels = new HashMap<>();
    private final CyclicDependencyInjectionMessageFormatter errorMessageFormatter = new
            SimpCyclicDependencyInjectionMessageFormatter();

    public SimpCircularDependencyInjectionScannerService(Set<ClassModel> classModels){
        classModels.stream()
                .map(CircularDependencyModel::new)
                .sorted(Comparator.comparing(model -> model.getAllRequiredDependencies().length))
                .forEach(model -> circularModels.put(model.getClassModel().getType(), model));
    }

    @Override
    public void lookForCircularDependencies() throws CircularDependencyInjectionException {
        for(Map.Entry<Class<?>, CircularDependencyModel> cyclicModelEntry : circularModels.entrySet()) {
            Class<?> rootClass = cyclicModelEntry.getValue()
                    .getClassModel()
                    .getType();

            Class<?>[] rootClassDependencies = cyclicModelEntry.getValue()
                    .getClassModel()
                    .getConstructor()
                    .getParameterTypes();

            this.lookForCircularDependenciesOnSubNodes(rootClass, rootClassDependencies);
        }
    }

    private void lookForCircularDependenciesOnSubNodes(Class<?> rootClass, Class<?>[] rootClassDependencies) throws CircularDependencyInjectionException {
        for (Class<?> dependency : rootClassDependencies) {
            Set<Class<?>> cyclicDependencies = this.lookUpForCircularDependenciesRecursively(
                    rootClass,
                    dependency,
                    new HashSet<>()
                    );

            if(cyclicDependencies != null){
                cyclicDependencies.add(dependency);
                Class<?>[] circularDependencies = cyclicDependencies.toArray(Class[]::new);
                this.failOnRootCDFound(circularDependencies);
            }
        }
    }

    private void failOnRootCDFound(Class<?>[] circularDependencies){
        String errorMessage = errorMessageFormatter.getRootNodeErrorMessage(circularDependencies);
        System.out.println(errorMessage);
        // throw new CircularDependencyInjectionException(errorMessage);
    }

    private Class<?>[] getClassDependencies(Class<?> cls){
        return Optional.of(circularModels.get(cls))
                .map(model -> model.getClassModel().getConstructor().getParameterTypes())
                .orElseThrow(() -> {
                    String errorMessage = String.format("""
                    A bean of type %s was required but none were present
                    """, cls);

                    return new IllegalArgumentException(errorMessage);
                });
    }

    private void failOnSubNodesCDFound(Class<?> rootClass, Class<?> causedBy, Set<Class<?>> circularDependencies){
        String errorMessage = errorMessageFormatter.getMessageOnSubNodesError(
                rootClass,
                causedBy,
                circularDependencies
        );

        System.out.println(errorMessage);
        //throw new CircularDependencyInjectionException(errorMessage);
    }

    /*
    A circular dependency on the root node can be spotted when the root node type is found somewhere else
    within the tree hierarchy.
    For sub nodes it works almost the same, since it checks whether the rootClass and sub node class are equals,
    a helper collection is needed to spot this circular dependency among sub nodes.
    */
    private Set<Class<?>> lookUpForCircularDependenciesRecursively(
            Class<?> rootClass,
            Class<?> currentClass,
            Set<Class<?>> subNodeCDHelper
            ){
        if(rootClass.equals(currentClass)) return new HashSet<>();

        Class<?>[] currentClassDependencies = this.getClassDependencies(currentClass);

        for(Class<?> dependency : currentClassDependencies) {
            Set<Class<?>> scanResult = this.lookUpForCircularDependenciesRecursively(rootClass, dependency, subNodeCDHelper);

            if(subNodeCDHelper.contains(dependency)){
                System.out.println("I've got it");
            }
            subNodeCDHelper.add(dependency);

            if(scanResult != null){
                scanResult.add(dependency);
                return scanResult;
            }
        }

        return null;
    }

}
