package com.glaze.autumn;

import com.glaze.autumn.circulardependency.model.CircularDependencyModel;
import com.glaze.autumn.circulardependency.service.SimpCircularDependencyDetectionService;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.instantiator.service.ClassInstantiationService;
import com.glaze.autumn.instantiator.service.SimpClassInstantiationService;
import com.glaze.autumn.clslocator.service.ClassLocatorService;
import com.glaze.autumn.clslocator.service.DirectoryClassLocatorService;
import com.glaze.autumn.clslocator.service.JarFileClassLocatorService;
import com.glaze.autumn.clscanner.service.ClassScannerService;
import com.glaze.autumn.clscanner.service.SimpClassScannerService;
import com.glaze.autumn.shared.constant.FileConstants;
import com.glaze.autumn.clslocator.enums.EnvironmentType;
import com.glaze.autumn.shared.exception.AutumnApplicationException;

import java.util.*;

@com.glaze.autumn.annotations.AutumnApplication
public class AutumnApplication {

    public static void main(String[] args) {
        run(AutumnApplication.class);
    }

    public static void run(Class<?> startUpClass){
        isStartUpClassValid(startUpClass);

        Environment environment = resolveEnvironmentForClass(startUpClass);
        ClassLocatorService locatorService = resolveClassLocatorServiceForType(environment.getType());

        Set<Class<?>> loadedClasses = locatorService.findAllProjectClasses(environment);
        ClassScannerService scannerService = new SimpClassScannerService(loadedClasses);

        Set<ClassModel> suitableClasses =  scannerService.findSuitableClasses();

        scanCircularDependencies(suitableClasses);

        ClassInstantiationService instantiationService = new SimpClassInstantiationService(suitableClasses);
        instantiationService.instantiateComponents();
    }

    private static void isStartUpClassValid(Class<?> cls) throws AutumnApplicationException{
        if(!cls.isAnnotationPresent(com.glaze.autumn.annotations.AutumnApplication.class)){
            String errorMessage = String.format("""
            Startup class %s must annotated with @AutumnApplication annotation""", cls.getTypeName());

            throw new AutumnApplicationException(errorMessage);
        }
    }

    private static Environment resolveEnvironmentForClass(Class<?> cls){
        String clsPath = cls.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getFile();

        EnvironmentType type = clsPath.endsWith(FileConstants.JAR_EXTENSION)
                ? EnvironmentType.JAR_FILE
                : EnvironmentType.DIRECTORY;

        return new Environment(clsPath, type);
    }

    private static ClassLocatorService resolveClassLocatorServiceForType(EnvironmentType type){
        return type == EnvironmentType.DIRECTORY
                ? new DirectoryClassLocatorService()
                : new JarFileClassLocatorService();
    }

    private static void scanCircularDependencies(Set<ClassModel> suitableClasses){
        List<CircularDependencyModel> models = suitableClasses.stream()
                .map(CircularDependencyModel::new)
                .sorted(Comparator.comparing(model -> model.getAllRequiredDependencies().length))
                .toList();

        Map<Class<?>, CircularDependencyModel> circularModels = new HashMap<>();
        for(CircularDependencyModel model : models){
            circularModels.put(model.getType(), model);
        }

        var circularDependencyDetectionService = new SimpCircularDependencyDetectionService(circularModels);
        circularDependencyDetectionService.scan();
    }

}
