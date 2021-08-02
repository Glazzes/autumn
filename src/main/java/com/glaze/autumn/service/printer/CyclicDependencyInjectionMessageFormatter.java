package com.glaze.autumn.service.printer;

public interface CyclicDependencyInjectionMessageFormatter {
    String formatMessage(Class<?>[] cyclicDependencies);
}
