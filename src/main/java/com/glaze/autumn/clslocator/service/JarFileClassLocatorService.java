package com.glaze.autumn.clslocator.service;

import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.constants.FileConstants;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileClassLocatorService implements ClassLocatorService {
    private final Set<Class<?>> suitableClasses = new HashSet<>();

    @Override
    public Set<Class<?>> findAllProjectClasses(Environment environment) {
        try{
            JarFile jarFile = new JarFile(new File(environment.getPath()));
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()){
                JarEntry entry = entries.nextElement();
                if(entry.isDirectory() || !entry.getName().endsWith(FileConstants.CLASS_EXTENSION)) continue;

                try{
                    String clsName = entry.getName()
                            .replaceAll("\\\\", ".")
                            .replaceAll("/", ".")
                            .replace(FileConstants.CLASS_EXTENSION, "");

                    Class<?> cls = Class.forName(clsName);
                    suitableClasses.add(cls);
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                }

            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return suitableClasses;
    }
}
