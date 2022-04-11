package com.glaze.autumn.services.clazzlocator;

import com.glaze.autumn.enums.FileExtension;
import com.glaze.autumn.models.Environment;
import com.glaze.autumn.exceptions.ClassLocationException;
import com.glaze.autumn.utils.ClassUtils;
import com.glaze.autumn.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
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

        if(basePackages == null) {
            return this.loadAllSourceCodeClasses(environment.getPath());
        }

        Collection<Class<?>> classes = new HashSet<>();
        Collection<JarFile> dependencies = FileUtils.getJarDependencies();

        ComponentScanLocatorService componentScanLocatorService = new ComponentScanLocatorService(basePackages);
        componentScanLocatorService.findSourceCodeClassesFromFileSystem(environment);
        Collection<Class<?>> sourceCodeClasses = ClassUtils.loadClasses(componentScanLocatorService.getClasses());

        classes.addAll(sourceCodeClasses);
        for (JarFile dependency : dependencies) {
            componentScanLocatorService.clear();
            componentScanLocatorService.findJarEntriesRecursively(dependency);
            Collection<Class<?>> jarClasses = ClassUtils.loadClasses(componentScanLocatorService.getClasses());
            classes.addAll(jarClasses);
        }

        return classes;
    }

    private Set<Class<?>> loadAllSourceCodeClasses(String path) {
        Path currentPath = Paths.get(path);
        try{
            return Files.walk(currentPath)
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().endsWith(FileExtension.CLASS.getValue()))
                    .map(p -> p.toString()
                            .replace(path, "")
                            .replaceAll("[\\\\/]", ".")
                            .replace(FileExtension.CLASS.getValue(), "")
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
