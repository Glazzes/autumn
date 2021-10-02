package com.glaze.autumn;

import com.glaze.autumn.circulardependency.model.CircularDependencyModel;
import com.glaze.autumn.circulardependency.service.SimpCircularDependencyDetectionService;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.instantiator.model.InstantiationQueuedModel;
import com.glaze.autumn.instantiator.service.ClassInstantiationService;
import com.glaze.autumn.instantiator.service.SimpClassInstantiationService;
import com.glaze.autumn.clslocator.service.ClassLocatorService;
import com.glaze.autumn.clslocator.service.DirectoryClassLocatorService;
import com.glaze.autumn.clslocator.service.JarFileClassLocatorService;
import com.glaze.autumn.clscanner.service.ClassScannerService;
import com.glaze.autumn.clscanner.service.SimpClassScannerService;
import com.glaze.autumn.shared.constant.FileConstants;
import com.glaze.autumn.clslocator.enums.EnvironmentType;
import com.glaze.autumn.test.One;

import java.util.*;

@com.glaze.autumn.annotations.AutumnApplication
public class AutumnApplication implements CommandLineRunner{
    private final One one;
    public AutumnApplication(One one){
        this.one = one;
    }

    @Override
    public void run() {
        System.out.println("Hello world");
    }

    public static void main(String[] args) {
        run(AutumnApplication.class);
    }

    public static void run(Class<?> startUpClass){
        Environment environment = resolveEnvironmentForClass(startUpClass);
        ClassLocatorService locatorService = resolveClassLocatorServiceForType(environment.getType());
        Set<Class<?>> loadedClasses = locatorService.findAllProjectClasses(environment);

        ClassScannerService scannerService = new SimpClassScannerService(loadedClasses);
        Set<ClassModel> suitableClasses =  scannerService.scan();
        scanCircularDependencies(suitableClasses, startUpClass);

        ClassInstantiationService instantiationService = new SimpClassInstantiationService(suitableClasses);
        instantiationService.instantiateComponents();
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

    private static void scanCircularDependencies(
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

    private static void runApplication(Class<?> startUpClass){
        SimpClassScannerService scannerService = new SimpClassScannerService();
        ClassModel model = scannerService.scanMainClasses(startUpClass);


    }

}
