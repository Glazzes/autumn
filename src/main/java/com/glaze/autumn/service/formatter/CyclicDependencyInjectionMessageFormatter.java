package com.glaze.autumn.service.formatter;

import java.util.Set;

public interface CyclicDependencyInjectionMessageFormatter {
    String getRootNodeErrorMessage(Class<?>[] circularDependencies);
    String getMessageOnSubNodesError(Class<?> rootClass, Class<?> causedBy, Set<Class<?>> circularDependencies);
}
