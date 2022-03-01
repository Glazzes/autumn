package com.glaze.autumn;

import static com.glaze.autumn.application.Runner.*;
import static com.glaze.autumn.application.EnvironmentResolver.*;
import static com.glaze.autumn.application.ClsLoader.*;

import com.glaze.autumn.circulardependency.service.SimpCircularDependencyCheckService;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.instantiator.model.InstantiationQueuedModel;
import com.glaze.autumn.instantiator.service.SimpClassInstantiationService;
import com.glaze.autumn.clscanner.service.SimpClassScannerService;
import com.glaze.autumn.test.One;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Autumn{

    private static final Logger logger = LogManager.getLogger(Autumn.class);

    public static void main(String[] args) {
        run(One.class);
    }

    public static void run(Class<?> startUpClass){
        logger.debug("Starting Application");
        Environment environment = resolveEnvironment(startUpClass);
        Set<Class<?>> loadedClasses = loadClasses(environment);

        var scannerService = new SimpClassScannerService(loadedClasses);
        Set<ClassModel> suitableClasses =  scannerService.scan();
        ClassModel mainModel = scannerService.scanMainClasses(startUpClass);
        scanCircularDependencies(startUpClass, suitableClasses);

        var instantiationService = new SimpClassInstantiationService(suitableClasses);
        instantiationService.instantiateComponents();
        var resolvedModel = instantiationService.instantiateMainClass(new InstantiationQueuedModel(mainModel));
        runApplication(resolvedModel);
    }

    private static void scanCircularDependencies(Class<?> startUpClass, Set<ClassModel> suitableClasses) {
        logger.debug("Look for circular dependencies in your project... \uD83D\uDCA4");
        var circularDependencyDetectionService = new SimpCircularDependencyCheckService(suitableClasses, startUpClass);
        circularDependencyDetectionService.checkProjectDependencies();
        logger.debug("No circular dependencies detected ✅");
    }

}
