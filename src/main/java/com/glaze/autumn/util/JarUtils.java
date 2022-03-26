package com.glaze.autumn.util;

import com.glaze.autumn.application.exception.AutumnApplicationException;
import com.glaze.autumn.clslocator.constants.FileConstants;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class JarUtils {

    public static JarFile convertEntryToFile(JarFile parentJar, JarEntry entry, Path destination) {
        try (var is = ((FileInputStream) parentJar.getInputStream(entry)).getChannel();
             var outChannel = new FileOutputStream(destination.toFile()).getChannel()
        ){
            ByteBuffer buffer = ByteBuffer.allocate(8096);
            while (is.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }

            return new JarFile(destination.toFile());
        }catch (IOException e) {
            throw new AutumnApplicationException("Could not write dependency jar to file system");
        }
    }

    public static Collection<JarEntry> getInnerJarEntries(JarFile jarFile) {
        return jarFile.stream()
                .filter(it -> it.getName().endsWith(FileConstants.JAR_EXTENSION))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public static Collection<String> getClassEntries(JarFile jarFile) {
        return jarFile.stream()
                .map(ZipEntry::getName)
                .filter(it -> it.endsWith(FileConstants.CLASS_EXTENSION))
                .map(it -> it.replaceAll("[\\\\/]", "."))
                .map(it -> it.replace(FileConstants.CLASS_EXTENSION, ""))
                .collect(Collectors.toCollection(HashSet::new));
    }

}
