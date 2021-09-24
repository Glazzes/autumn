package com.glaze.autumn.clslocator.service;

import com.glaze.autumn.clslocator.model.Environment;

import java.util.Set;

public interface ClassLocatorService {
    Set<Class<?>> findAllProjectClasses(Environment environment);
}
