package com.glaze.autumn.clslocator.service;

import com.glaze.autumn.clslocator.model.Environment;

import java.util.Collection;
import java.util.Set;

public interface ClassLocatorService {
    Collection<Class<?>> getProjectClasses(Environment environment);
}
