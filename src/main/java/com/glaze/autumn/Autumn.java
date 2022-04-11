package com.glaze.autumn;

import com.glaze.autumn.annotations.MainMethod;
import com.glaze.autumn.enums.FileExtension;
import com.glaze.autumn.exceptions.AutumnApplicationException;
import com.glaze.autumn.exceptions.InvalidMethodSignatureException;
import com.glaze.autumn.services.circulardependencyscanner.GraphCircularDependencyCheckService;
import com.glaze.autumn.models.ClassModel;
import com.glaze.autumn.enums.EnvironmentType;
import com.glaze.autumn.models.Environment;
import com.glaze.autumn.services.clazzlocator.ClassLocatorService;
import com.glaze.autumn.services.clazzlocator.DirectoryClassLocatorService;
import com.glaze.autumn.services.clazzlocator.JarFileClassLocatorService;
import com.glaze.autumn.services.clazzinstantiator.SimpClassInstantiationService;
import com.glaze.autumn.services.clazzscanner.SimpClassScannerService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Autumn {
    private static final Logger logger = java.util.logging.Logger.getLogger(Autumn.class.getCanonicalName());

    public static void run(Class<?> startUpClass){
        logger.log(Level.INFO, "Starting application \uD83D\uDD25!");
        Environment environment = resolveEnvironment(startUpClass);
        Collection<Class<?>> loadedClasses = locateClasses(environment, startUpClass);

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

        EnvironmentType type = clsPath.endsWith(FileExtension.JAR.getValue())
                ? EnvironmentType.JAR_FILE
                : EnvironmentType.DIRECTORY;

        String emoji = type.toString().equalsIgnoreCase("directory") ? " 📁" : " \uD83D\uDDC3️";
        logger.info("Running as a " + type.toString().toLowerCase() + emoji);
        return new Environment(clsPath, type);
    }

    private static Collection<Class<?>> locateClasses(Environment environment, Class<?> startUpClass) {
        ClassLocatorService locatorService = environment.getType() == EnvironmentType.JAR_FILE
                ? new JarFileClassLocatorService(startUpClass)
                : new DirectoryClassLocatorService(startUpClass);

        Collection<Class<?>> projectClasses = locatorService.getProjectClasses(environment);
        projectClasses.add(startUpClass);
        return projectClasses;
    }

    private static void scanCircularDependencies(Class<?> startUpClass, Set<ClassModel> suitableClasses) {
        var circularDependencyDetectionService = new GraphCircularDependencyCheckService(suitableClasses, startUpClass);
        circularDependencyDetectionService.checkProjectDependencies();
    }

    private static Object instantiateClasses(Class<?> startupClass, Set<ClassModel> suitableClasses) {
        var instantiationService = new SimpClassInstantiationService(suitableClasses);
        instantiationService.instantiate();

        return instantiationService.getInstances()
                .stream()
                .filter(instance -> instance.getClass().equals(startupClass))
                .findFirst()
                .orElseThrow(() -> new AutumnApplicationException("Main class not found"));
    }

    private static void runApplication(Object instance){
        Class<?> instanceType = instance.getClass();
        logger.info("Application started \uD83C\uDF1F");

        Method mainMethod = null;
        for(Method method : instanceType.getDeclaredMethods()) {
            if(method.isAnnotationPresent(MainMethod.class)) {
                if(method.getParameterCount() == 0 && method.getReturnType().equals(void.class)) {
                    mainMethod = method;
                    mainMethod.setAccessible(true);
                    break;
                }else {
                    throw new InvalidMethodSignatureException("""
                    Methods annotated with @MainMethod must take no arguments and a return type of void
                    """);
                }
            }
        }

        if(mainMethod != null) {
            try{
                mainMethod.invoke(instance);
            }catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

}
