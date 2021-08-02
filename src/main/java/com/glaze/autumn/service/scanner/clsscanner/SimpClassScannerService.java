package com.glaze.autumn.service.scanner.clsscanner;

import com.glaze.autumn.configuration.annotation.AnnotationConfiguration;
import com.glaze.autumn.configuration.annotation.BasicContainerConfiguration;
import com.glaze.autumn.model.ClassModel;
import com.glaze.autumn.shared.annotation.Autowired;
import com.glaze.autumn.shared.annotation.AutumnApplication;
import com.glaze.autumn.shared.annotation.Bean;
import com.glaze.autumn.shared.annotation.PostConstruct;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpClassScannerService implements ClassScannerService {
    private final Set<Class<?>> locatedClasses;
    private final AnnotationConfiguration annotationConfiguration;

    public SimpClassScannerService(Set<Class<?>> locatedClasses){
        this.locatedClasses = locatedClasses;
        this.annotationConfiguration = new BasicContainerConfiguration();
    }

    public SimpClassScannerService(Set<Class<?>> locatedClasses, AnnotationConfiguration configuration){
        this.locatedClasses = locatedClasses;
        this.annotationConfiguration = configuration;
    }

    @Override
    public Set<ClassModel> getSuitableClasses() {
        return this.locatedClasses.stream()
                .filter(this::filterForClassesOnly)
                .filter(this::filterForComponentClasses)
                .map(cls -> {
                    ClassModel clsModel = new ClassModel();
                    clsModel.setType(cls);
                    clsModel.setAutowiredFields(this.getAutowiredFields(cls));
                    clsModel.setConstructor(this.getSuitableConstructor(cls));
                    clsModel.setPostConstruct(this.getPostConstructMethod(cls));
                    clsModel.setBeans(this.getBeans(cls));
                    clsModel.setIsStartUpClass(this.isStartUpClass(cls));

                    return clsModel;
                })
                .collect(Collectors.toSet());
    }


    private boolean filterForClassesOnly(Class<?> cls){
        return !cls.isInterface()
               && !cls.isEnum()
               && !cls.isAnnotation()
               && !cls.isPrimitive();
    }

    private boolean filterForComponentClasses(Class<?> cls){
        return annotationConfiguration.getComponentAnnotations()
                .stream()
                .anyMatch(cls::isAnnotationPresent);
    }

    private boolean isStartUpClass(Class<?> cls){
        return cls.isAnnotationPresent(AutumnApplication.class);
    }

    private Field[] getAutowiredFields(Class<?> cls){
        Set<Field> autowiredFields = new HashSet<>();
        for (Field field : cls.getDeclaredFields()) {
            if(!field.isAnnotationPresent(Autowired.class)
               || field.getType().equals(Void.class)
            ){
                continue;
            }

            field.setAccessible(true);
            autowiredFields.add(field);
        }

        return autowiredFields.size() > 0
               ? autowiredFields.toArray(Field[]::new)
               : null;
    }

    private Constructor<?> getSuitableConstructor(Class<?> cls){
        Constructor<?> targetConstructor = cls.getDeclaredConstructors()[0];
        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
            if(constructor.isAnnotationPresent(Autowired.class)){
                constructor.setAccessible(true);
                targetConstructor = constructor;
                break;
            }
        }

        return targetConstructor;
    }

    private Method getPostConstructMethod(Class<?> cls){
        Method postConstructMethod = null;
        for (Method method : cls.getDeclaredMethods()) {
            if(method.getParameterCount() != 0
               || !method.isAnnotationPresent(PostConstruct.class)
               || !(method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class) )
            ){
                continue;
            }

            method.setAccessible(true);
            postConstructMethod = method;
            break;
        }

        return postConstructMethod;
    }

    private Method[] getBeans(Class<?> cls){
        Set<Method> beans = new HashSet<>();
        for (Method method : cls.getDeclaredMethods()) {
            if(method.getParameterCount() != 0
               || !method.isAnnotationPresent(Bean.class)
               || method.getReturnType().equals(void.class)
               || method.getReturnType().equals(Void.class)
            ){
                continue;
            }

            method.setAccessible(true);
            beans.add(method);
        }

        return beans.size() > 0
               ? beans.toArray(Method[]::new)
               : null;
    }

}
