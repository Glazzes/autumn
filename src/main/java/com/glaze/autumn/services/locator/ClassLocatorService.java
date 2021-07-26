package com.glaze.autumn.services.locator;

import com.glaze.autumn.models.Environment;

import java.util.Set;

public interface ClassLocatorService {
    Set<Class<?>> findAllProjectClasses(Environment environment);
}
