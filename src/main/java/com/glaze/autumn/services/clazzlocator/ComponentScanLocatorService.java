package com.glaze.autumn.services.clazzlocator;

import com.glaze.autumn.enums.FileExtension;
import com.glaze.autumn.exceptions.AutumnApplicationException;
import com.glaze.autumn.models.Environment;
import com.glaze.autumn.utils.JarUtils;

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
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class ComponentScanLocatorService {
    private final String[] packageRoutes;
    private final Pattern[] packagePatterns;
    private final Set<String> classes = new HashSet<>();
    private final String tempDir = System.getProperty("java.io.tmpdir");

    private final String fileSeparator = System.getProperty("file.separator");
    private final String folderRegex = "(\\\\\\\\|/)";

    public ComponentScanLocatorService(String[] packages) {
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
        Path jarLocation = Paths.get(environment.getPath());
        try(Stream<Path> paths = Files.walk(jarLocation)){
            Set<String> entries = paths.filter(it -> !Files.isDirectory(it))
                    .map(Path::toString)
                    .filter(s -> s.endsWith(FileExtension.CLASS.getValue()))
                    .collect(Collectors.toCollection(HashSet::new));

            for(String entryName : entries) {
                String className = this.convertEntryToClassName(entryName);
                if(className != null) {
                    this.classes.add(className);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void findSourceCodeClassesFromJar(JarFile file) {
        Set<String> classes = file.stream()
                .map(ZipEntry::getName)
                .filter(it -> it.endsWith(FileExtension.JAR.getValue()))
                .collect(Collectors.toCollection(HashSet::new));

        for(String entryName : classes) {
            String className = this.convertEntryToClassName(entryName);
            if(className != null) {
                this.classes.add(className);
            }
        }
    }

    public void findJarEntriesRecursively(JarFile parentJarFile) {
        Enumeration<JarEntry> entries = parentJarFile.entries();
        Path newParentDestination = Paths.get(tempDir + "/" + UUID.randomUUID() + FileExtension.JAR.getValue());

        while (entries.hasMoreElements()) {
            JarEntry currentEntry = entries.nextElement();
            String currentEntryName = currentEntry.getName();

            if(currentEntryName.endsWith(FileExtension.CLASS.getValue())) {
                String className = this.convertEntryToClassName(currentEntryName);
                if(className != null) {
                    this.classes.add(className);
                }
            }

            if(currentEntryName.endsWith(FileExtension.JAR.getValue())) {
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

    private String convertEntryToClassName(String currentEntryName) {
        String separatorRegex = String.format("\\%s", fileSeparator);
        for(int i = 0; i < packageRoutes.length; i++) {
            Matcher matcher = packagePatterns[i].matcher(currentEntryName);

            if(matcher.find()) {
                return currentEntryName
                    .replaceAll("^.*" + separatorRegex + "(" +  packageRoutes[i] +")", "$1")
                    .replaceFirst(FileExtension.CLASS.getValue(), "")
                    .replaceAll(separatorRegex, ".");
            }
        }

        return null;
    }

    public Set<String> getClasses() {
        return this.classes;
    }

    public void clear() {
        this.classes.clear();
    }
}
