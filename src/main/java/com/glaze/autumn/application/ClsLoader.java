package com.glaze.autumn.application;

import com.glaze.autumn.clslocator.enums.EnvironmentType;
import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.clslocator.service.ClassLocatorService;
import com.glaze.autumn.clslocator.service.DirectoryClassLocatorService;
import com.glaze.autumn.clslocator.service.JarFileClassLocatorService;

import java.util.Set;

public interface ClsLoader {
    static Set<Class<?>> loadClasses(Environment environment){
        ClassLocatorService locatorService = environment.getType() == EnvironmentType.JAR_FILE
                ? new JarFileClassLocatorService()
                : new DirectoryClassLocatorService();

        return locatorService.findAllProjectClasses(environment);
    }
}
