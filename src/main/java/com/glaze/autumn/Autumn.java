package com.glaze.autumn;

import com.glaze.autumn.annotations.Autowired;
import com.glaze.autumn.annotations.Component;
import com.glaze.autumn.application.CommandLineRunner;
import com.glaze.autumn.application.exception.AutumnApplicationException;
import com.glaze.autumn.circulardependency.service.GraphCircularDependencyCheckService;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.clslocator.constants.FileConstants;
import com.glaze.autumn.clslocator.enums.EnvironmentType;
import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.clslocator.service.ClassLocatorService;
import com.glaze.autumn.clslocator.service.DirectoryClassLocatorService;
import com.glaze.autumn.clslocator.service.JarFileClassLocatorService;
import com.glaze.autumn.instantiator.service.SimpClassInstantiationService;
import com.glaze.autumn.clscanner.service.SimpClassScannerService;
import com.glaze.autumn.test.Two;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Autumn implements CommandLineRunner {
    @Autowired private Two two;

    private static final Logger logger = java.util.logging.Logger.getLogger(Autumn.class.getCanonicalName());

    @Override
    public void run() {
        System.out.println(two);
    }

    public static void main(String[] args) {
        run(Autumn.class);
    }

    public static void run(Class<?> startUpClass){
        logger.log(Level.INFO, "Starting application!");
        Environment environment = resolveEnvironment(startUpClass);
        Collection<Class<?>> loadedClasses = loadClasses(environment, startUpClass);

        var scannerService = new SimpClassScannerService(loadedClasses);
        Set<ClassModel> suitableClasses =  scannerService.scanProjectClasses();
        scanCircularDependencies(startUpClass, suitableClasses);

        Object mainClassInstance = instantiateClasses(startUpClass, suitableClasses);
        runApplication(mainClassInstance);
    }

    private static Environment resolveEnvironment(Class<?> startUpClass) {
        String clsPath = startUpClass.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getFile();

        EnvironmentType type = clsPath.endsWith(FileConstants.JAR_EXTENSION)
                ? EnvironmentType.JAR_FILE
                : EnvironmentType.DIRECTORY;

        String emoji = type.toString().toLowerCase().equals("directory") ? " 📁" : " \uD83D\uDDC3️";
        logger.info("Running as a " + type.toString().toLowerCase() + emoji);
        return new Environment(clsPath, type);
    }

    private static Collection<Class<?>> loadClasses(Environment environment, Class<?> startUpClass) {
        ClassLocatorService locatorService = environment.getType() == EnvironmentType.JAR_FILE
                ? new JarFileClassLocatorService(startUpClass)
                : new DirectoryClassLocatorService(startUpClass);

        Collection<Class<?>> loadedClasses = locatorService.getProjectClasses(environment);
        logger.info("Project classes loaded successfully ✅");
        return loadedClasses;
    }

    private static void scanCircularDependencies(Class<?> startUpClass, Set<ClassModel> suitableClasses) {
        logger.log(Level.INFO, "Look for circular dependencies in your project... \uD83D\uDCA4");
        var circularDependencyDetectionService = new GraphCircularDependencyCheckService(suitableClasses, startUpClass);
        circularDependencyDetectionService.checkProjectDependencies();
        logger.log(Level.INFO ,"No circular dependencies detected ✅");
    }

    private static Object instantiateClasses(Class<?> startupClass, Set<ClassModel> suitableClasses) {
        var instantiationService = new SimpClassInstantiationService(suitableClasses);
        instantiationService.instantiate();

        return instantiationService.getInstances()
                .stream()
                .filter(instance -> instance.getClass().equals(startupClass))
                .findFirst()
                .orElseThrow(() -> new AutumnApplicationException("Could not find main class"));
    }

    private static void runApplication(Object instance){
        Class<?> instanceType = instance.getClass();
        logger.info("Application started \uD83C\uDF1F");

        if(CommandLineRunner.class.isAssignableFrom(instanceType)){
            try{
                Method runMethod = instanceType.getMethod("run");
                if(runMethod.getReturnType().equals(void.class)
                        && runMethod.getParameterCount() == 0){
                    runMethod.invoke(instance);
                }
            }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
                e.printStackTrace();
            }
        }
    }

}
