package com.glaze.autumn.instantiator.service;

import com.glaze.autumn.annotations.*;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.instantiator.exception.ComponentNotFoundException;
import com.glaze.autumn.instantiator.model.InstantiationQueuedModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class SimpClassInstantiationService implements ClassInstantiationService {
    private final LinkedList<InstantiationQueuedModel> queuedModels;
    private final LinkedHashMap<String, Object> availableInstances = new LinkedHashMap<>();

    public SimpClassInstantiationService(Set<ClassModel> clsModels){
        this.queuedModels = clsModels.stream()
                .sorted(Comparator.comparing(model -> model.getConstructor().getParameterCount()))
                .map(InstantiationQueuedModel::new)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public void instantiateComponents() {
        int counter = 0;
        while (!queuedModels.isEmpty()){
            if(counter > 100) break;
            InstantiationQueuedModel queuedModel = queuedModels.removeFirst();
            resolveDependencies(queuedModel);
            counter++;
        }
    }

    public void resolveDependencies(InstantiationQueuedModel instantiationModel){
        int parameterCount = instantiationModel.getClassModel()
                .getConstructor()
                .getParameterCount();

        if(parameterCount == 0){
            this.instantiateModelWithNoArgsConstructor(instantiationModel);
        }else{
            this.resolveConstructorDependencies(instantiationModel);
        }

        this.attemptInstantiation(instantiationModel);
    }

    private void instantiateModelWithNoArgsConstructor(InstantiationQueuedModel model){
        try{
            Object instance = model.getClassModel()
                    .getConstructor()
                    .newInstance();

            model.setInstance(instance);
            if(!model.hasAutowiredFieldsResolved()){
                this.resolveAutowiredFieldsDependencies(model);
                this.assignAutowiredFieldDependencies(model);
            }

            if(model.isModelResolved()){
                String instanceId = this.generateClassId(model.getClassModel());
                this.availableInstances.put(instanceId, instance);
                this.instantiateBeans(model.getClassModel().getBeans(), model.getInstance());
            }
        }catch (InvocationTargetException | IllegalAccessException | InstantiationException e){
            e.printStackTrace();
        }
    }

    private void attemptInstantiation(InstantiationQueuedModel model){
        try{
            this.resolveConstructorDependencies(model);

            if(model.hasConstructorDependenciesResolved()){
                Constructor<?> classConstructor = model.getClassModel().getConstructor();
                Object[] params = model.getConstructorDependencyInstances();
                model.setInstance(classConstructor.newInstance(params));

                this.resolveAutowiredFieldsDependencies(model);
                if(model.hasAutowiredFieldsResolved()){
                    this.assignAutowiredFieldDependencies(model);
                }

                if(model.isModelResolved()){
                    String classId = this.generateClassId(model.getClassModel());
                    this.availableInstances.put(classId, model.getInstance());
                    this.instantiateBeans(model.getBeans(), model.getInstance());
                }
            }else{
                this.queuedModels.addLast(model);
            }
        }catch (InstantiationException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    private void resolveConstructorDependencies(InstantiationQueuedModel model){
        for(int i = 0; i < model.getConstructorDependencies().length; i++){
            Class<?> dependencyType = model.getConstructorDependencies()[i];
            for (Object dependency : availableInstances.values()) {
                if(dependency.getClass().isAssignableFrom(dependencyType)
                        && model.getConstructorDependencyInstances()[i] == null
                ){
                    model.getConstructorDependencyInstances()[i] = dependency;
                }
            }
        }
    }

    private void resolveAutowiredFieldsDependencies(InstantiationQueuedModel model){
        Field[] autowiredFields = model.getAutowiredFields();
        if(autowiredFields == null) return;

        for(int i = 0; i < autowiredFields.length; i++){
            Field currentField = autowiredFields[i];
            Class<?> fieldType =currentField.getType();

            if(currentField.isAnnotationPresent(Qualifier.class)){
                Qualifier qualifier = currentField.getAnnotation(Qualifier.class);
                String componentId = String.format(
                        "%s-%s",
                        currentField.getType().getTypeName(),
                        qualifier.id()
                );

                Object dependency = this.availableInstances.get(componentId);
                if(dependency == null){
                    String errorMessage = String.format("""
                    A bean of type %s with id %s was required by %s but could not be found or instantiated
                    """, currentField.getType().getTypeName(), qualifier.id(), model.getClassModel().getType());
                    throw new ComponentNotFoundException(errorMessage);
                }

                model.getAutowiredFieldDependencyInstances()[i] = dependency;
                return;
            }

            for (Object dependency : this.availableInstances.values()) {
                if(dependency.getClass().isAssignableFrom(fieldType)){
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
        String component = model.getType().getTypeName();
        String id = UUID.randomUUID().toString()
                .replaceAll("-", "")
                .substring(0, 10);

        if(classType.isAnnotationPresent(Service.class)){
            Service serviceAnnotation = classType.getAnnotation(Service.class);
            if(!serviceAnnotation.id().isBlank()){
                id = serviceAnnotation.id();
            }
        }

        if(model.getType().isAnnotationPresent(Repository.class)){
            Repository repositoryAnnotation = classType.getAnnotation(Repository.class);
            if(!repositoryAnnotation.id().isBlank()){
                id = repositoryAnnotation.id();
            }
        }

        if(classType.isAnnotationPresent(Component.class)){
            Component componentAnnotation = classType.getAnnotation(Component.class);
            if(!componentAnnotation.id().isBlank()){
                id = componentAnnotation.id();
            }
        }

        return String.format("%s-%s", component, id);
    }

    @Override
    public void instantiateBeans(Method[] beans, Object instance) {
        if(beans == null) return;

        try{
            for (Method bean : beans) {
                Object beanInstance = bean.invoke(instance);
                String beanId = generateBeanId(bean);
                this.availableInstances.put(beanId, beanInstance);
            }
        }catch (IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    private String generateBeanId(Method bean){
        String beanType = bean.getReturnType().getTypeName();
        String id = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 10);

        Bean beanAnnotation = bean.getAnnotation(Bean.class);
        if(!beanAnnotation.id().isBlank()){
            id = beanAnnotation.id();
        }

        return String.format("%s-%s", beanType, id);
    }

    @Override
    public Map<String, Object> getInstances() {
        return this.availableInstances;
    }
}