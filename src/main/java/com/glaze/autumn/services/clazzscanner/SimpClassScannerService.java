package com.glaze.autumn.services.clazzscanner;

import com.glaze.autumn.exceptions.InvalidMethodSignatureException;
import com.glaze.autumn.models.ClassModel;
import com.glaze.autumn.annotations.Autowired;
import com.glaze.autumn.annotations.Bean;
import com.glaze.autumn.annotations.PostConstruct;
import com.glaze.autumn.annotations.configuration.Annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SimpClassScannerService implements ClassScannerService {
    private final Collection<Class<?>> locatedClasses;
    private final Annotations annotations;
    private final Logger logger = Logger.getLogger(SimpClassScannerService.class.getCanonicalName());

    public SimpClassScannerService(Collection<Class<?>> locatedClasses){
        this.locatedClasses = locatedClasses;
        this.annotations = new Annotations();
    }

    @Override
    public Set<ClassModel> scanProjectClasses() {
        Set<ClassModel> model = this.locatedClasses.stream()
                .filter(this::findClassFiles)
                .filter(this::findComponentClasses)
                .map(this::newClassModelInstance)
                .collect(Collectors.toSet());

        logger.info("Project classes loaded successfully ✅");
        return model;
    }

    private ClassModel newClassModelInstance(Class<?> cls){
        ClassModel clsModel = new ClassModel();
        clsModel.setType(cls);
        clsModel.setAutowiredFields(this.getAutowiredFields(cls));
        clsModel.setConstructor(this.getSuitableConstructor(cls));
        clsModel.setPostConstruct(this.getPostConstructMethod(cls));
        clsModel.setBeans(this.getBeanMethods(cls));

        return clsModel;
    }

    private boolean findClassFiles(Class<?> cls){
        return !cls.isInterface()
                && !cls.isEnum()
                && !cls.isAnnotation()
                && !cls.isAnonymousClass()
                && !cls.isPrimitive();
    }

    private boolean findComponentClasses(Class<?> cls){
        return this.annotations.getAnnotations()
                .stream()
                .anyMatch(cls::isAnnotationPresent);
    }

    private Field[] getAutowiredFields(Class<?> cls) {
        Set<Field> autowiredFields = new HashSet<>();
        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)
                && !field.getType().equals(Void.class)
                && !field.getType().equals(void.class)
                && !field.getType().isPrimitive()
            ) {
                field.setAccessible(true);
                autowiredFields.add(field);
            }
        }

        return new ArrayList<>(autowiredFields)
            .toArray(new Field[autowiredFields.size()]);
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
                    throw new InvalidMethodSignatureException("Methods annotated with @PostConstruct must take no arguments and must not have a return value");
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
                    throw new InvalidMethodSignatureException("Methods annotated with @Bean must not take arguments and should not return void.class or Void.class");
                }

                method.setAccessible(true);
                beans.add(method);
            }
        }

        return new ArrayList<>(beans)
            .toArray(new Method[beans.size()]);
    }

}
