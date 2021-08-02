package com.glaze.autumn.service.scanner.cyclicscanner;

import com.glaze.autumn.model.ClassModel;
import com.glaze.autumn.model.CyclicDependencyModel;
import com.glaze.autumn.service.printer.CyclicDependencyInjectionMessageFormatter;
import com.glaze.autumn.service.printer.SimpCyclicDependencyInjectionMessageFormatter;
import com.glaze.autumn.shared.exception.CyclicDependencyInjectionException;

import java.util.*;

public class SimpCyclicDependencyScannerService implements CyclicDependencyScannerService {
    private final Map<Class<?>, CyclicDependencyModel> cyclicModels = new HashMap<>();
    private final CyclicDependencyInjectionMessageFormatter errorMessageFormatter = new
            SimpCyclicDependencyInjectionMessageFormatter();

    public SimpCyclicDependencyScannerService(Set<ClassModel> classModels){
        classModels.stream()
                .map(CyclicDependencyModel::new)
                .sorted(Comparator.comparing(model -> model.getAllRequiredDependencies().length))
                .forEach(model -> cyclicModels.put(model.getClassModel().getType(), model));
    }

    @Override
    public void scan() throws CyclicDependencyInjectionException {
        for(Map.Entry<Class<?>, CyclicDependencyModel> cyclicModelEntry : cyclicModels.entrySet()) {
            Class<?> rootClass = cyclicModelEntry.getValue()
                    .getClassModel()
                    .getType();

            Class<?>[] rootClassDependencies = cyclicModelEntry.getValue()
                    .getClassModel()
                    .getConstructor()
                    .getParameterTypes();

            for (Class<?> dependency : rootClassDependencies) {
                Collection<Class<?>> cyclicDependencies = this.scanForCyclicDependencies(rootClass, dependency);

                if(cyclicDependencies != null){
                    cyclicDependencies.add(dependency);
                    Collections.reverse((List<Class<?>>) cyclicDependencies);
                    Class<?>[] dependencyArr = cyclicDependencies.toArray(Class[]::new);

                    String errorMessage = errorMessageFormatter.formatMessage(dependencyArr);
                    throw new CyclicDependencyInjectionException(errorMessage);
                }
            }
        }
    }

    public Collection<Class<?>> scanForCyclicDependencies(Class<?> rootClass, Class<?> currentClass){
        if(rootClass.equals(currentClass)) return new ArrayList<>();

        Class<?>[] currentClassDependencies = Optional.of(cyclicModels.get(currentClass))
                .map(model -> model.getClassModel().getConstructor().getParameterTypes())
                .orElseThrow(() -> {
                    String errorMessage = String.format("""
                    A bean of type %s was required but none were present
                    """, currentClass);

                    return new IllegalArgumentException(errorMessage);
                });

        for (Class<?> dependency : currentClassDependencies) {
            Collection<Class<?>> scanResult = this.scanForCyclicDependencies(rootClass, dependency);
            if(scanResult != null){
                scanResult.add(dependency);
                return scanResult;
            }
        }

        return null;
    }

}
