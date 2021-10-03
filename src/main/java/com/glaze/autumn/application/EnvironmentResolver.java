package com.glaze.autumn.application;

import com.glaze.autumn.clslocator.enums.EnvironmentType;
import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.shared.constant.FileConstants;

public interface EnvironmentResolver {

    static Environment resolveInvironment(Class<?> startUpClass){
        String clsPath = startUpClass.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getFile();

        EnvironmentType type = clsPath.endsWith(FileConstants.JAR_EXTENSION)
                ? EnvironmentType.JAR_FILE
                : EnvironmentType.DIRECTORY;

        return new Environment(clsPath, type);
    }

}
