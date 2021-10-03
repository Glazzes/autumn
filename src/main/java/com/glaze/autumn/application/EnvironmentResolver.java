package com.glaze.autumn.application;

import com.glaze.autumn.clslocator.enums.EnvironmentType;
import com.glaze.autumn.clslocator.model.Environment;
import com.glaze.autumn.constants.FileConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

public interface EnvironmentResolver {
    Logger logger = LogManager.getLogger(EnvironmentResolver.class);

    static Environment resolveEnvironment(Class<?> startUpClass){
        String clsPath = startUpClass.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getFile();

        EnvironmentType type = clsPath.endsWith(FileConstants.JAR_EXTENSION)
                ? EnvironmentType.JAR_FILE
                : EnvironmentType.DIRECTORY;

        logger.debug("Running as a " + type.toString().toLowerCase(Locale.ROOT));
        return new Environment(clsPath, type);
    }

}
