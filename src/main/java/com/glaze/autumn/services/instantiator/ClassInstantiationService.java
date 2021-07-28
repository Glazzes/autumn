package com.glaze.autumn.services.instantiator;

import java.lang.reflect.Method;
import java.util.Map;

public interface ClassInstantiationService {
    void instantiateComponents();
    void instantiateBeans(Method[] beans, Object instance);
    Map<String, Object> getInstances();
}
