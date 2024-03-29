package com.glaze.autumn.services.clazzlocator;

import com.glaze.autumn.exceptions.AutumnApplicationException;
import com.glaze.autumn.models.Environment;
import com.glaze.autumn.utils.ClassUtils;
import com.glaze.autumn.utils.JarUtils;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class JarFileClassLocatorService implements ClassLocatorService {
    private final Class<?> startUpClass;

    public JarFileClassLocatorService(Class<?> startUpClass) {
        this.startUpClass = startUpClass;
    }

    @Override
    public Collection<Class<?>> getProjectClasses(Environment environment) {
        try{
            JarFile jarFile = new JarFile(new File(environment.getPath()));
            String[] basePackages =  ClassUtils.getBasePackages(this.startUpClass);

            if(basePackages == null) {
                return this.loadClassesFromSingleJar(jarFile);
            }

            ComponentScanLocatorService componentScanLocatorService = new ComponentScanLocatorService(basePackages);
            componentScanLocatorService.findSourceCodeClassesFromJar(jarFile);
            componentScanLocatorService.findJarEntriesRecursively(jarFile);
            return ClassUtils.loadClasses(componentScanLocatorService.getClasses());
        }catch (Exception e) {
            throw new AutumnApplicationException("We could not locate project's path");
        }
    }

    private Set<Class<?>> loadClassesFromSingleJar(JarFile jarFile) {
        Collection<String> classNames = JarUtils.getClassEntries(jarFile);

        return classNames.stream()
                .map(it -> {
                    try {
                        return Class.forName(it);
                    }catch (ClassNotFoundException e) {
                        throw new AutumnApplicationException("We could not load " + it  + " class");
                    }
                })
                .collect(Collectors.toSet());
    }
}
