package com.glaze.autumn.services.clazzinstantiator;

import java.util.Collection;

public interface ClassInstantiationService {
    void instantiate();
    Collection<Object> getInstances();
}
