package com.glaze.autumn.utils;

import com.glaze.autumn.enums.FileExtension;
import com.glaze.autumn.exceptions.AutumnApplicationException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.jar.JarFile;

public class FileUtils {
    public static Collection<JarFile> getJarDependencies() {
        Collection<JarFile> jarFiles = new HashSet<>();
        try{
            String classPath = System.getProperty("java.class.path");
            String[] jars = classPath.split(":");

            for(String resource : jars) {
                if(resource.endsWith(FileExtension.JAR.getValue())) {
                    jarFiles.add(new JarFile(resource));
                }
            }

            return jarFiles;
        }catch (IOException e) {
            throw new AutumnApplicationException("Could not load project dependencies");
        }
    }

}
