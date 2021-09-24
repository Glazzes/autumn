package com.glaze.autumn.clscanner.service;

import com.glaze.autumn.clscanner.exception.InvalidMethodSignatureException;
import com.glaze.autumn.configuration.annotation.AnnotationConfiguration;
import com.glaze.autumn.configuration.annotation.BasicContainerConfiguration;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.annotations.Autowired;
import com.glaze.autumn.annotations.AutumnApplication;
import com.glaze.autumn.annotations.Bean;
import com.glaze.autumn.annotations.PostConstruct;

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
    public Set<ClassModel> findSuitableClasses() {
        return this.locatedClasses.stream()
                .filter(this::findClassFiles)
                .filter(this::findComponentClasses)
                .map(cls -> {
                    ClassModel clsModel = new ClassModel();
                    clsModel.setType(cls);
                    clsModel.setAutowiredFields(this.getAutowiredFields(cls));
                    clsModel.setConstructor(this.getSuitableConstructor(cls));
                    clsModel.setPostConstruct(this.getPostConstructMethod(cls));
                    clsModel.setBeans(this.getBeanMethods(cls));
                    clsModel.setIsStartUpClass(this.isStartUpClass(cls));

                    return clsModel;
                })
                .collect(Collectors.toSet());
    }

    private boolean findClassFiles(Class<?> cls){
        return !cls.isInterface()
               && !cls.isEnum()
               && !cls.isAnnotation()
               && !cls.isPrimitive();
    }

    private boolean findComponentClasses(Class<?> cls){
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
            if(method.isAnnotationPresent(PostConstruct.class)){
                if(method.getParameterCount() != 0
                        || !(method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class) )
                ){
                    throw new InvalidMethodSignatureException("""
                    Methods annotated with @PostConstruct must take no arguments and must not have a return value
                    """);
                }

                method.setAccessible(true);
                postConstructMethod = method;
                break;
            }
        }

        return postConstructMethod;
    }

    private Method[] getBeanMethods(Class<?> cls){
        Set<Method> beans = new HashSet<>();
        for (Method method : cls.getDeclaredMethods()) {
            if(method.isAnnotationPresent(Bean.class)){
                if(method.getParameterCount() != 0
                        || method.getReturnType().equals(void.class)
                        || method.getReturnType().equals(Void.class)
                ){
                    throw new InvalidMethodSignatureException("""
                    Methods annotated with @Bean must not take arguments and should not return
                    void.class or Void.class
                    """);
                }

                method.setAccessible(true);
                beans.add(method);
            }
        }

        return beans.size() > 0 ? beans.toArray(Method[]::new) : null;
    }

}