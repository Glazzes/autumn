package com.glaze.autumn.instantiator.service;

import com.glaze.autumn.annotations.*;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.instantiator.exception.ComponentNotFoundException;
import com.glaze.autumn.instantiator.exception.DuplicatedIdentifierException;
import com.glaze.autumn.instantiator.model.InstantiationQueuedModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class SimpClassInstantiationService implements ClassInstantiationService, MissingDependencyScanner {
    private final Queue<InstantiationQueuedModel> queuedModels;
    private final Map<String, Object> availableInstances = new HashMap<>();
    private final Logger logger = LogManager.getLogger(SimpClassInstantiationService.class);

    public SimpClassInstantiationService(Set<ClassModel> clsModels){
        this.queuedModels = clsModels.stream()
                .sorted(Comparator.comparing(model -> model.getConstructor().getParameterCount()))
                .map(InstantiationQueuedModel::new)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    public InstantiationQueuedModel instantiateMainClass(InstantiationQueuedModel mainModel){
        logger.debug("Instantiating " + mainModel.getClassModel().getType() + " class");
        try{
            this.resolveConstructorDependencies(mainModel);
            if(mainModel.hasConstructorDependenciesResolved()){
                this.attemptConstructorInstantiation(mainModel);
            }

            this.resolveAutowiredFieldsDependencies(mainModel);
            if(mainModel.hasAutowiredFieldsResolved()){
                this.assignAutowiredFieldDependencies(mainModel);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(!mainModel.isModelResolved()){
            mainModel.onUnresolvedAutowiredFieldDependencies();
            mainModel.onUnresolvedConstructorDependencies();
        }

        logger.debug("Main class has been instantiated correctly");
        return mainModel;
    }

    @Override
    public void instantiateComponents() {
        logger.debug("Instantiating classes");
        int counter = 0;
        while (!queuedModels.isEmpty()){
            if(counter > 1000) break;
            InstantiationQueuedModel queuedModel = queuedModels.poll();
            attemptInstantiation(queuedModel);
            counter++;
        }

        if(queuedModels.size() > 0){
            scanMissingAutowiredDependencies();
            scanMissingConstructorDependencies();
        }

        logger.debug("Project classes have been instantiated correctly");
    }

    private void attemptInstantiation(InstantiationQueuedModel model){
        try{
            this.resolveConstructorDependencies(model);
            this.attemptConstructorInstantiation(model);

            this.resolveAutowiredFieldsDependencies(model);
            if(model.hasAutowiredFieldsResolved()){
                this.assignAutowiredFieldDependencies(model);
            }

            if(model.isModelResolved()){
                String classId = this.generateClassId(model.getClassModel());
                if(availableInstances.containsKey(classId)){
                    String errorMessage = String.format("""
                    Can not instantiate %s because id "%s" is already use by another component
                    """, model.getClassModel().getType(), classId);

                    throw new DuplicatedIdentifierException(errorMessage);
                }

                this.availableInstances.put(classId, model.getInstance());
                this.instantiateBeans(model.getBeans(), model.getInstance());

                this.invokePostConstructMethod(model);
            }else{
                queuedModels.add(model);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void resolveConstructorDependencies(InstantiationQueuedModel model){
        for(int i = 0; i < model.getConstructorParameterTypes().length; i++){
            Class<?> dependencyType = model.getConstructorParameterTypes()[i];
            Object instance = model.getConstructorDependencyInstances()[i];
            Annotation[] dependencyAnnotations = model.getConstructorParameterAnnotations()[i];

            if(dependencyAnnotations != null && dependencyAnnotations.length > 0){
                Qualifier qualifier = null;
                for(Annotation ann : dependencyAnnotations){
                    if(Qualifier.class.isAssignableFrom(ann.getClass())){
                        qualifier = (Qualifier) ann;
                        break;
                    }
                }

                if(qualifier != null && instance == null){
                    Object availableInstance = availableInstances.get(qualifier.id());
                    if(availableInstance != null){
                        model.getConstructorDependencyInstances()[i] = availableInstance;
                    }

                    continue;
                }
            }

            for (Object dependency : availableInstances.values()) {
                if(dependencyType.isAssignableFrom(dependency.getClass()) && instance == null){
                    model.getConstructorDependencyInstances()[i] = dependency;
                }
            }
        }
    }

    private void attemptConstructorInstantiation(InstantiationQueuedModel model) throws Exception{
        if(model.hasConstructorDependenciesResolved() && model.getInstance() == null) {
            Constructor<?> classConstructor = model.getClassModel().getConstructor();
            Object[] params = model.getConstructorDependencyInstances();
            if(params.length == 0){
                Object instance = classConstructor.newInstance();
                model.setInstance(instance);
                return;
            }

            Object instance = classConstructor.newInstance(params);
            model.setInstance(instance);
        }
    }

    public void invokePostConstructMethod(InstantiationQueuedModel model){
        try{
            Object instance = model.getInstance();
            Method postConstruct = model.getClassModel()
                    .getPostConstruct();

            if(postConstruct != null){
                postConstruct.invoke(instance);
            }
        }catch (IllegalAccessException|InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public void resolveAutowiredFieldsDependencies(InstantiationQueuedModel model){
        Field[] autowiredFields = model.getAutowiredFields();
        if(autowiredFields == null) return;

        for(int i = 0; i < autowiredFields.length; i++){
            Field currentField = autowiredFields[i];
            Class<?> fieldType = currentField.getType();

            if(currentField.isAnnotationPresent(Qualifier.class)){
                Qualifier qualifier = currentField.getAnnotation(Qualifier.class);
                String componentId = qualifier.id();

                Object dependency = availableInstances.get(componentId);
                if(dependency != null){
                    Class<?> dependencyType = dependency.getClass();
                    if(fieldType.isAssignableFrom(dependencyType)){
                        model.getAutowiredFieldDependencyInstances()[i] = dependency;
                    }
                }

                continue;
            }

            for (Object dependency : this.availableInstances.values()) {
                if(dependency.getClass().isAssignableFrom(fieldType)
                   && model.getAutowiredFieldDependencyInstances()[i] == null){
                    model.getAutowiredFieldDependencyInstances()[i] = dependency;
                }
            }

        }
    }

    private void assignAutowiredFieldDependencies(InstantiationQueuedModel model){
        if(model.getAutowiredFields() == null) return;
        if(model.getAutowiredFields().length == 0) return;

        Object instance = model.getInstance();
        Field[] autowiredFields = model.getAutowiredFields();
        for(int i = 0; i < autowiredFields.length; i++){
            Field autowiredField = autowiredFields[i];
            Object autowiredFieldDependency = model.getAutowiredFieldDependencyInstances()[i];
            try {
                autowiredField.set(instance, autowiredFieldDependency);
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }
        }
    }

    private String generateClassId(ClassModel model){
        Class<?> classType = model.getType();
        String componentName = model.getType().getTypeName();

        if(classType.isAnnotationPresent(Service.class)){
            Service serviceAnnotation = classType.getAnnotation(Service.class);
            if(!serviceAnnotation.id().isBlank()){
                return serviceAnnotation.id();
            }
        }

        if(model.getType().isAnnotationPresent(Repository.class)){
            Repository repositoryAnnotation = classType.getAnnotation(Repository.class);
            if(!repositoryAnnotation.id().isBlank()){
                return repositoryAnnotation.id();
            }
        }

        if(classType.isAnnotationPresent(Component.class)){
            Component componentAnnotation = classType.getAnnotation(Component.class);
            if(!componentAnnotation.id().isBlank()){
                return componentAnnotation.id();
            }
        }

        return componentName;
    }

    @Override
    public void instantiateBeans(Method[] beans, Object instance) {
        if(beans == null) return;

        try{
            for (Method method : beans) {
                Object beanInstance = method.invoke(instance);
                String beanId = method.getName();

                boolean exists = availableInstances.containsKey(beanId);
                if(exists){
                    throw new DuplicatedIdentifierException("There's already a bean with id " + beanId);
                }

                this.availableInstances.put(beanId, beanInstance);
            }
        }catch (IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    @Override
    public void scanMissingAutowiredDependencies() {
        for(InstantiationQueuedModel model : queuedModels){
            if(model.getAutowiredFields() == null) continue;

            Field[] autowiredFields = model.getAutowiredFields();
            for(int field = 0; field < autowiredFields.length; field++){
                if(model.getAutowiredFieldDependencyInstances()[field] == null){
                    if(autowiredFields[field].isAnnotationPresent(Qualifier.class)){
                        Qualifier qualifier = autowiredFields[field].getAnnotation(Qualifier.class);
                        String errorMessage = String.format(
                                "%s required a bean of type %s with id %s that could not be found, consider declaring one",
                                model.getClassModel().getType(),
                                autowiredFields[field].getType().getTypeName(),
                                qualifier.id()
                        );

                        throw new ComponentNotFoundException(errorMessage);
                    }

                    String errorMessage = String.format(
                            "%s required a bean of type %s that could not be found, consider declaring one",
                            model.getClassModel().getType(),
                            autowiredFields[field].getType().getTypeName()
                    );

                    throw new ComponentNotFoundException(errorMessage);
                }
            }
        }
    }

    @Override
    public void scanMissingConstructorDependencies() {
        for(InstantiationQueuedModel model : queuedModels){
            Class<?>[] constructorDependencies = model.getConstructorParameterTypes();
            for(int dep = 0; dep < constructorDependencies.length; dep++){
                Object dependency = model.getConstructorDependencyInstances()[dep];
                if(dependency == null){
                    String errorMessage = String.format(
                    "%s's required a bean of type %s that could not be found, consider declaring one.",
                           model.getClassModel().getType(),
                           model.getConstructorParameterTypes()[dep]
                    );

                    throw new ComponentNotFoundException(errorMessage);
                }
            }
        }
    }

    @Override
    public Map<String, Object> getInstances() {
        return this.availableInstances;
    }
}
