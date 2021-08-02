package com.glaze.autumn.service.scanner.cyclicscanner;

import com.glaze.autumn.shared.exception.CyclicDependencyInjectionException;

public interface CyclicDependencyScannerService {
    void scan() throws CyclicDependencyInjectionException;
}
