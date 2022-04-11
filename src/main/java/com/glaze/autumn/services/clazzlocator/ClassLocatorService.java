package com.glaze.autumn.services.clazzlocator;

import com.glaze.autumn.models.Environment;

import java.util.Collection;

public interface ClassLocatorService {
    Collection<Class<?>> getProjectClasses(Environment environment);
}
