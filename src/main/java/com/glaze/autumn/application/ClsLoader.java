package com.glaze.autumn.application;

import com.glaze.autumn.clslocator.enums.EnvironmentType;
import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.clslocator.service.ClassLocatorService;
import com.glaze.autumn.clslocator.service.DirectoryClassLocatorService;
import com.glaze.autumn.clslocator.service.JarFileClassLocatorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public interface ClsLoader {
    Logger logger = LogManager.getLogger(ClsLoader.class);

    static Set<Class<?>> loadClasses(Environment environment){
        ClassLocatorService locatorService = environment.getType() == EnvironmentType.JAR_FILE
                ? new JarFileClassLocatorService()
                : new DirectoryClassLocatorService();

        Set<Class<?>> loadedClasses = locatorService.findAllProjectClasses(environment);
        logger.debug("Project classes loaded successfully");
        return loadedClasses;
    }
}
