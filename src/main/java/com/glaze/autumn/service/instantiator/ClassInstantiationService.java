package com.glaze.autumn.service.instantiator;

import java.lang.reflect.Method;
import java.util.Map;

public interface ClassInstantiationService {
    void instantiateComponents();
    void instantiateBeans(Method[] beans, Object instance);
    Map<String, Object> getInstances();
}
