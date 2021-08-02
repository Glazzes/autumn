package com.glaze.autumn.service.instantiator;

import com.glaze.autumn.model.ClassModel;
import com.glaze.autumn.model.InstantiationQueuedModel;
import com.glaze.autumn.shared.annotation.*;

import java.lang.reflect.Constructor;
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

    public void resolveDependencies(InstantiationQueuedModel model){
        if(model.getClassModel().getConstructor().getParameterCount() == 0){
            this.instantiateModelWithNoArgsConstructor(model);
        }else{
            this.resolveConstructorDependencies(model);
        }

        if(model.isResolved()){
            this.instantiateModelIfResolved(model);
        }else {
            this.addModelBackToQueue(model);
        }
    }

    private void instantiateModelWithNoArgsConstructor(InstantiationQueuedModel model){
        try{
            Object instance = model.getClassModel().getConstructor().newInstance();
            String instanceId = this.generateClassId(model.getClassModel());
            this.availableInstances.put(instanceId, instance);
            this.instantiateBeans(model.getClassModel().getBeans(), instance);
        }catch (InvocationTargetException | IllegalAccessException | InstantiationException e){
            e.printStackTrace();
        }
    }

    private void resolveConstructorDependencies(InstantiationQueuedModel model){
        for(int i = 0; i < model.getDependencyTypes().length; i++){
            Class<?> dependencyType = model.getDependencyTypes()[i];
            for (Map.Entry<String, Object> entry : availableInstances.entrySet()) {
                if(entry.getValue().getClass().isAssignableFrom(dependencyType)
                        && model.getDependencyInstances()[i] == null
                ){
                    model.getDependencyInstances()[i] = entry.getValue();
                }
            }
        }
    }

    private void instantiateModelIfResolved(InstantiationQueuedModel model){
        try{
            Constructor<?> classConstructor = model.getClassModel().getConstructor();
            Object[] params = model.getDependencyInstances();
            Object newInstance = classConstructor.newInstance(params);
            String componentId = this.generateClassId(model.getClassModel());
            availableInstances.put(componentId, newInstance);
            this.instantiateBeans(model.getClassModel().getBeans(), newInstance);
        }catch (InstantiationException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    private void addModelBackToQueue(InstantiationQueuedModel model){
        this.queuedModels.addLast(model);
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
