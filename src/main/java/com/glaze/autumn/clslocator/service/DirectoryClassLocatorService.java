package com.glaze.autumn.clslocator.service;

import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.clslocator.constants.FileConstants;
import com.glaze.autumn.clslocator.exception.ClassLocationException;
import com.glaze.autumn.util.ClassUtils;
import com.glaze.autumn.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class DirectoryClassLocatorService implements ClassLocatorService {
    private final Class<?> startUpClass;

    public DirectoryClassLocatorService(Class<?> startUpClass) {
        this.startUpClass = startUpClass;
    }

    @Override
    public Collection<Class<?>> getProjectClasses(Environment environment) {
        String[] basePackages = ClassUtils.getBasePackages(startUpClass);
        Collection<Class<?>> sourceCodeClasses = this.loadClassesFromSourceCode(environment.getPath());

        if(basePackages == null) {
            return sourceCodeClasses;
        }

        Collection<JarFile> dependencies = FileUtils.getJarDependencies(this.startUpClass);
        for (JarFile dependency : dependencies) {
            ComponentScanService componentScanService = new ComponentScanService(basePackages);
            componentScanService.findJarEntriesRecursively(dependency);
            Collection<Class<?>> jarClasses = ClassUtils.loadClasses(componentScanService.getClasses());
            sourceCodeClasses.addAll(jarClasses);
        }

        return sourceCodeClasses;
    }

    private Set<Class<?>> loadClassesFromSourceCode(String path) {
        Path currentPath = Paths.get(path);
        try{
            return Files.walk(currentPath)
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().endsWith(FileConstants.CLASS_EXTENSION))
                    .map(p -> p.toString()
                            .replace(path, "")
                            .replaceAll("[\\\\/]", ".")
                            .replace(FileConstants.CLASS_EXTENSION, "")
                    )
                    .map(clsName -> {
                        try{
                            return Class.forName(clsName);
                        }catch (ClassNotFoundException e){
                            throw new ClassLocationException(e.getMessage(), e);
                        }
                    })
                    .collect(Collectors.toSet());
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

}
