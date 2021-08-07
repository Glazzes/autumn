package com.glaze.autumn.service.scanner.circularscanner;

import com.glaze.autumn.model.ClassModel;
import com.glaze.autumn.model.CircularDependencyModel;
import com.glaze.autumn.service.formatter.CyclicDependencyInjectionMessageFormatter;
import com.glaze.autumn.service.formatter.SimpCyclicDependencyInjectionMessageFormatter;
import com.glaze.autumn.shared.exception.CircularDependencyInjectionException;

import java.util.*;

public class SimpCircularDependencyDetectionService implements CircularDependencyDetectionService {
    private final Map<Class<?>, CircularDependencyModel> circularModels = new HashMap<>();
    private final CyclicDependencyInjectionMessageFormatter errorMessageFormatter = new
            SimpCyclicDependencyInjectionMessageFormatter();

    public SimpCircularDependencyDetectionService(Set<ClassModel> classModels){
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
            List<Class<?>> circularDependencies = this.lookUpForCircularDependenciesRecursively(
                    rootClass,
                    dependency,
                    new LinkedList<>()
                    );

            if(circularDependencies != null){
                circularDependencies.add(dependency);
                Collections.reverse(circularDependencies);
                Class<?>[] circularDependenciesArr = circularDependencies.toArray(Class[]::new);
                this.failOnRootCDFound(circularDependenciesArr);
            }
        }
    }

    private void failOnRootCDFound(Class<?>[] circularDependencies) {
        String errorMessage = errorMessageFormatter.getRootNodeErrorMessage(circularDependencies);
        throw new CircularDependencyInjectionException(errorMessage);
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

    private void failOnSubNodesCDFound(Class<?> rootClass, Class<?> causedBy, List<Class<?>> circularDependencies){
        Class<?>[] circularDependencyArr = circularDependencies.toArray(Class<?>[]::new);
        String errorMessage = errorMessageFormatter.getMessageOnSubNodesError(
                rootClass,
                causedBy,
                circularDependencyArr
        );

        System.out.println(Arrays.toString(circularDependencyArr));
        throw new CircularDependencyInjectionException(errorMessage);
    }

    /*
    A circular dependency on the root node can be spotted when the root node type is found somewhere else
    within the tree hierarchy.

    A circular dependency can also appear among sub nodes where the root class is not present, therefore causing
    a StackOverflowError because this method checks whether the root class and current class are equals.
    In order to spot a circular dependency on sub nodes a helper collection is used to save all classes in the
    current tree branch if no circular dependency was present, this helper collection gets cleaned up for the
    next branch to use.
    */
    private List<Class<?>> lookUpForCircularDependenciesRecursively(
            Class<?> rootClass,
            Class<?> currentClass,
            LinkedList<Class<?>> subNodeCDHelper
    ){
        if(rootClass.equals(currentClass)) return new ArrayList<>();

        Class<?>[] currentClassDependencies = this.getClassDependencies(currentClass);
        for(Class<?> dependency : currentClassDependencies) {
            if(subNodeCDHelper.contains(dependency)){
                this.failOnSubNodesCDFound(rootClass, dependency, subNodeCDHelper);
            }
            subNodeCDHelper.add(dependency);

            List<Class<?>> scanResult = this.lookUpForCircularDependenciesRecursively(rootClass, dependency, subNodeCDHelper);

            if(scanResult != null){
                scanResult.add(dependency);
                return scanResult;
            }
        }

        subNodeCDHelper.removeLast();
        return null;
    }

}
