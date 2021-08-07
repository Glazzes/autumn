package com.glaze.autumn.service.formatter;

public interface CyclicDependencyInjectionMessageFormatter {
    String getRootNodeErrorMessage(Class<?>[] circularDependencies);
    String getMessageOnSubNodesError(Class<?> rootClass, Class<?> causedBy, Class<?>[] circularDependencies);
}
