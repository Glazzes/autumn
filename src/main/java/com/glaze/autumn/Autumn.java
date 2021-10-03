package com.glaze.autumn;

import static com.glaze.autumn.application.Runner.*;
import static com.glaze.autumn.application.EnvironmentResolver.*;
import static com.glaze.autumn.application.ClsLoader.*;
import static com.glaze.autumn.application.CDScanner.*;

import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.instantiator.model.InstantiationQueuedModel;
import com.glaze.autumn.instantiator.service.SimpClassInstantiationService;
import com.glaze.autumn.clscanner.service.SimpClassScannerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Autumn {
    private static final Logger logger = LogManager.getLogger(Autumn.class);

    public static void main(String[] args) {
        run(Autumn.class);
    }

    public static void run(Class<?> startUpClass){
        logger.debug("Starting Application");
        Environment environment = resolveEnvironment(startUpClass);
        Set<Class<?>> loadedClasses = loadClasses(environment);

        var scannerService = new SimpClassScannerService(loadedClasses);
        Set<ClassModel> suitableClasses =  scannerService.scan();
        ClassModel mainModel = scannerService.scanMainClasses(startUpClass);
        scanCircularDependencies(suitableClasses, startUpClass);

        var instantiationService = new SimpClassInstantiationService(suitableClasses);
        instantiationService.instantiateComponents();
        var resolvedModel = instantiationService.instantiateMainClass(new InstantiationQueuedModel(mainModel));
        runApplication(resolvedModel);
    }

}
