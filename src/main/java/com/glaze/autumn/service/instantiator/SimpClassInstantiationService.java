package com.glaze.autumn.service.instantiator;

import com.glaze.autumn.model.ClassModel;
import com.glaze.autumn.model.InstantiationQueuedModel;
import com.glaze.autumn.shared.annotation.*;

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
        if(instantiationModel.getClassModel().getConstructor().getParameterCount() == 0){
            this.instantiateModelWithNoArgsConstructor(instantiationModel);
        }else{
            this.resolveConstructorDependencies(instantiationModel);
        }

        this.attemptToInstantiateModel(instantiationModel);
    }

    private void instantiateModelWithNoArgsConstructor(InstantiationQueuedModel model){
        try{
            Object instance = model.getClassModel().getConstructor().newInstance();
            model.setClsModelTypeInstance(instance);

            if(!model.areAutowireFieldsDependenciesResolved()){
                this.resolveAutowiredFieldsDependencies(model);
                this.assignAutowiredFieldDependencies(model);
            }

            if(model.isModelResolved()){
                String instanceId = this.generateClassId(model.getClassModel());
                this.availableInstances.put(instanceId, instance);
                this.instantiateBeans(model.getClassModel().getBeans(), model.getClsModelTypeInstance());
            }
        }catch (InvocationTargetException | IllegalAccessException | InstantiationException e){
            e.printStackTrace();
        }
    }

    private void attemptToInstantiateModel(InstantiationQueuedModel model){
        try{
            this.resolveConstructorDependencies(model);

            if(model.areConstructorDependenciesResolved()){
                Constructor<?> classConstructor = model.getClassModel().getConstructor();
                Object[] params = model.getConstructorDependencyInstances();
                model.setClsModelTypeInstance(classConstructor.newInstance(params));

                this.resolveAutowiredFieldsDependencies(model);
                if(model.areAutowireFieldsDependenciesResolved()){
                    this.assignAutowiredFieldDependencies(model);
                }

                if(model.isModelResolved()){
                    String classId = this.generateClassId(model.getClassModel());
                    this.availableInstances.put(classId, model.getClsModelTypeInstance());
                    this.instantiateBeans(model.getBeans(), model.getClsModelTypeInstance());
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
            Class<?> fieldType = autowiredFields[i].getType();
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

        Object instance = model.getClsModelTypeInstance();
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

        if(model.getType().isAnnotationPresent(Service.class)){
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

        if(model.getType().isAnnotationPresent(Component.class)){
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
