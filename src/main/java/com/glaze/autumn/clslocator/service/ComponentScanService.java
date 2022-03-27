package com.glaze.autumn.clslocator.service;

import com.glaze.autumn.application.exception.AutumnApplicationException;
import com.glaze.autumn.clslocator.constants.FileConstants;
import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.util.JarUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class ComponentScanService {
    private final String[] packageRoutes;
    private final Pattern[] packagePatterns;
    private final Set<String> classes = new HashSet<>();
    private final String tempDir = System.getProperty("java.io.tmpdir");

    private final String folderRegex = "(\\\\\\\\|/)";

    public ComponentScanService(String[] packages) {
        this.packageRoutes = Arrays.stream(packages)
                .map(it -> it.replaceAll("\\.", folderRegex))
                .toArray(String[]::new);

        this.packagePatterns = Arrays.stream(packages)
                .map(it -> it.replaceAll("\\.", folderRegex))
                .map(it -> it + folderRegex)
                .map(Pattern::compile)
                .toArray(Pattern[]::new);
    }

    public void findSourceCodeClassesFromFileSystem(Environment environment) {
        try{
            Files.walk(Paths.get(environment.getPath()))
                    .filter(it -> !Files.isDirectory(it))
                    .map(Path::toString)
                    .filter(s -> s.endsWith(FileConstants.CLASS_EXTENSION))
                    .collect(Collectors.toCollection(HashSet::new))
                    .forEach(this::addToClasses);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void findSourceCodeClassesFromJar(JarFile file) {
        file.stream()
                .map(ZipEntry::getName)
                .filter(it -> it.endsWith(FileConstants.JAR_EXTENSION))
                .collect(Collectors.toCollection(HashSet::new))
                .forEach(this::addToClasses);
    }

    public void findJarEntriesRecursively(JarFile parentJarFile) {
        Iterator<JarEntry> entries = parentJarFile.entries().asIterator();
        Path newParentDestination = Paths.get(tempDir + "/" + UUID.randomUUID() + FileConstants.JAR_EXTENSION);

        while (entries.hasNext()) {
            JarEntry currentEntry = entries.next();
            String currentEntryName = currentEntry.getName();

            if(currentEntryName.endsWith(FileConstants.CLASS_EXTENSION)) {
                this.addToClasses(currentEntryName);
            }

            if(currentEntryName.endsWith(FileConstants.JAR_EXTENSION)) {
                JarFile newParentJar = JarUtils.convertEntryToFile(parentJarFile, currentEntry, newParentDestination);
                this.findJarEntriesRecursively(newParentJar);
            }
        }

        try{
            Files.deleteIfExists(newParentDestination);
        }catch (IOException e){
            throw new AutumnApplicationException("Could not delete temporary file");
        }
    }

    private void addToClasses(String currentEntryName) {
        for(int i = 0; i < packageRoutes.length; i++) {
            Matcher matcher = packagePatterns[i].matcher(currentEntryName);

            if(matcher.find()) {
                String className = currentEntryName
                        .replaceAll("^.*/(" +  packageRoutes[i] +")", "$1")
                        .replaceFirst(FileConstants.CLASS_EXTENSION, "")
                        .replaceAll("(\\\\|/)", ".");

                this.classes.add(className);
                break;
            }
        }
    }

    public Set<String> getClasses() {
        return this.classes;
    }

    public void clear() {
        this.classes.clear();
    }
}
