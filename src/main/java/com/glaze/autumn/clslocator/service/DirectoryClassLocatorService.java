package com.glaze.autumn.clslocator.service;

import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.constants.FileConstants;
import com.glaze.autumn.clslocator.exception.ClassLocationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public class DirectoryClassLocatorService implements ClassLocatorService {

    @Override
    public Set<Class<?>> findAllProjectClasses(Environment environment) {
        Set<String> classNames = findAllClassNames(environment.getPath());
        return loadAllProjectClasses(classNames);
    }

    private Set<String> findAllClassNames(String path){
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
                 .collect(Collectors.toSet());
        }catch (IOException e){
           e.printStackTrace();
           return null;
        }
    }

    private Set<Class<?>> loadAllProjectClasses(Set<String> classNames){
        if(classNames == null){
            throw new ClassLocationException("No classes were loaded for this project");
        }

        return classNames.stream()
                .map(clsName -> {
                    try{
                        return Class.forName(clsName);
                    }catch (ClassNotFoundException e){
                        throw new ClassLocationException(e.getMessage(), e);
                    }
                })
                .collect(Collectors.toSet());
    }

}
