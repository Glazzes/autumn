package com.glaze.autumn.application;

import com.glaze.autumn.circulardependency.model.CircularDependencyModel;
import com.glaze.autumn.circulardependency.service.SimpCircularDependencyDetectionService;
import com.glaze.autumn.clscanner.model.ClassModel;

import java.util.*;

public interface CDScanner {
    static void scanCircularDependencies(
            Set<ClassModel> suitableClasses,
            Class<?> startUpClass
    ){
        List<CircularDependencyModel> models = suitableClasses.stream()
                .map(CircularDependencyModel::new)
                .sorted(Comparator.comparing(model -> model.getAllRequiredDependencies().length))
                .toList();

        Map<Class<?>, CircularDependencyModel> circularModels = new HashMap<>();
        for(CircularDependencyModel model : models){
            circularModels.put(model.getType(), model);
        }

        var circularDependencyDetectionService = new SimpCircularDependencyDetectionService(circularModels, startUpClass);
        circularDependencyDetectionService.scan();
    }
}
