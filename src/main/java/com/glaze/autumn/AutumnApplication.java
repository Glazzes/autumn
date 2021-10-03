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

import java.util.*;

@com.glaze.autumn.annotations.AutumnApplication
public class AutumnApplication{

    public static void main(String[] args) {
        run(AutumnApplication.class);
    }

    public static void run(Class<?> startUpClass){
        Environment environment = resolveInvironment(startUpClass);
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
