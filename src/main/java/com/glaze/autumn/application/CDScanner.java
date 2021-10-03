package com.glaze.autumn.application;

import com.glaze.autumn.circulardependency.model.CircularDependencyModel;
import com.glaze.autumn.circulardependency.service.SimpCircularDependencyDetectionService;
import com.glaze.autumn.clscanner.model.ClassModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public interface CDScanner {
    Logger logger = LogManager.getLogger(CDScanner.class);

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

        logger.debug("Scanning for circular dependencies");
        var circularDependencyDetectionService = new SimpCircularDependencyDetectionService(circularModels, startUpClass);
        circularDependencyDetectionService.scan();
        logger.debug("No circular dependencies detected");
    }
}
