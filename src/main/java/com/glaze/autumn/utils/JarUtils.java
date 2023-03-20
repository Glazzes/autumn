package com.glaze.autumn.utils;

import com.glaze.autumn.enums.FileExtension;
import com.glaze.autumn.exceptions.AutumnApplicationException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class JarUtils {

    public static JarFile convertEntryToFile(JarFile parentJar, JarEntry entry, Path destination) {
        try (FileChannel is = ((FileInputStream) parentJar.getInputStream(entry)).getChannel();
             FileChannel outChannel = new FileOutputStream(destination.toFile()).getChannel()
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

    public static Collection<String> getClassEntries(JarFile jarFile) {
        return jarFile.stream()
                .map(ZipEntry::getName)
                .filter(it -> it.endsWith(FileExtension.CLASS.getValue()))
                .map(it -> it.replaceAll("[\\\\/]", "."))
                .map(it -> it.replace(FileExtension.CLASS.getValue(), ""))
                .collect(Collectors.toCollection(HashSet::new));
    }

}
