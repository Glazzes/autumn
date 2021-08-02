package com.glaze.autumn.service.locator;

import com.glaze.autumn.model.Environment;

import java.util.Set;

public interface ClassLocatorService {
    Set<Class<?>> findAllProjectClasses(Environment environment);
}
