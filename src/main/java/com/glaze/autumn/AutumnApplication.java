package com.glaze.autumn;

import com.glaze.autumn.model.ClassModel;
import com.glaze.autumn.model.Environment;
import com.glaze.autumn.service.instantiator.ClassInstantiationService;
import com.glaze.autumn.service.instantiator.SimpClassInstantiationService;
import com.glaze.autumn.service.locator.ClassLocatorService;
import com.glaze.autumn.service.locator.DirectoryClassLocatorService;
import com.glaze.autumn.service.locator.JarFileClassLocatorService;
import com.glaze.autumn.service.scanner.clsscanner.ClassScannerService;
import com.glaze.autumn.service.scanner.clsscanner.SimpClassScannerService;
import com.glaze.autumn.service.scanner.cyclicscanner.CyclicDependencyScannerService;
import com.glaze.autumn.service.scanner.cyclicscanner.SimpCyclicDependencyScannerService;
import com.glaze.autumn.shared.constant.FileConstants;
import com.glaze.autumn.shared.enums.EnvironmentType;
import com.glaze.autumn.shared.exception.AutumnApplicationException;

import java.util.Set;

@com.glaze.autumn.shared.annotation.AutumnApplication
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
        Set<ClassModel> suitableClasses =  scannerService.getSuitableClasses();

        CyclicDependencyScannerService cyclicDependencyScannerService = new SimpCyclicDependencyScannerService(suitableClasses);
        cyclicDependencyScannerService.scan();

        ClassInstantiationService instantiationService = new SimpClassInstantiationService(suitableClasses);
        instantiationService.instantiateComponents();
    }

    private static void isStartUpClassValid(Class<?> cls) throws AutumnApplicationException{
        if(!cls.isAnnotationPresent(com.glaze.autumn.shared.annotation.AutumnApplication.class)){
            String errorMessage = String.format("""
            Startup class %s must annotated with @AutumnApplication annotation        
            """, cls.getTypeName());

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
}
