package com.glaze.autumn.util;

import com.glaze.autumn.application.exception.AutumnApplicationException;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarFile;

public class FileUtils {
    private static final String JAR_FILE_PREFIX = "jar:file:";

    public static Collection<JarFile> getJarDependencies(Class<?> clazz) {
        try{
            Collection<JarFile> jarFiles = new HashSet<>();
            Enumeration<URL> resources = clazz.getClassLoader()
                    .getResources("");

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getPath().startsWith(JAR_FILE_PREFIX)) {
                    String path = resource.getPath()
                            .replaceAll("!.*$", "");

                    jarFiles.add(new JarFile(path));
                }
            }

            return jarFiles;
        }catch (IOException e) {
            throw new AutumnApplicationException("Could not load project dependencies");
        }
    }

}
