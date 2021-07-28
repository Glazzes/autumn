package com.glaze.autumn;

import com.glaze.autumn.models.Environment;
import com.glaze.autumn.services.instantiator.ClassInstantiationService;
import com.glaze.autumn.services.instantiator.SimpClassInstantiationService;
import com.glaze.autumn.services.locator.ClassLocatorService;
import com.glaze.autumn.services.locator.DirectoryClassLocatorService;
import com.glaze.autumn.services.locator.JarFileClassLocatorService;
import com.glaze.autumn.services.scanner.ClassScannerService;
import com.glaze.autumn.services.scanner.SimpClassScannerService;
import com.glaze.autumn.shared.constants.FileConstants;
import com.glaze.autumn.shared.enums.EnvironmentType;

import java.util.Set;

public class AutumnApplication {
    public static void main(String[] args) {
        run(AutumnApplication.class);
    }

    public static void run(Class<?> startUpClass){
        Environment environment = resolveEnvironmentForClass(startUpClass);
        ClassLocatorService locatorService = resolveClassLocatorServiceForType(environment.getType());

        Set<Class<?>> loadedClasses = locatorService.findAllProjectClasses(environment);
        ClassScannerService scannerService = new SimpClassScannerService(loadedClasses);

        System.out.println(scannerService.getSuitableClasses());

        ClassInstantiationService instantiationService = new SimpClassInstantiationService(
                scannerService.getSuitableClasses()
        );

        instantiationService.instantiateComponents();

        System.out.println(instantiationService.getInstances());
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
}
