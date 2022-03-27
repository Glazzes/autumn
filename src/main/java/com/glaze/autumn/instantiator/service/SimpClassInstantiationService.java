package com.glaze.autumn.instantiator.service;

import com.glaze.autumn.annotations.Bean;
import com.glaze.autumn.annotations.Component;
import com.glaze.autumn.annotations.Repository;
import com.glaze.autumn.annotations.Service;
import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.dependencyresolver.service.DependencyResolverService;
import com.glaze.autumn.dependencyresolver.service.MissingDependencyHandler;
import com.glaze.autumn.dependencyresolver.service.SimpDependencyResolverService;
import com.glaze.autumn.dependencyresolver.service.SimpMissingDependencyHandler;
import com.glaze.autumn.instantiator.exception.DuplicatedIdentifierException;
import com.glaze.autumn.instantiator.model.InstantiationModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SimpClassInstantiationService implements ClassInstantiationService {
    private final Queue<InstantiationModel> instantiationModels;
    private final Map<String, Object> availableInstances = new HashMap<>();
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final DependencyResolverService dependencyResolverService = new SimpDependencyResolverService();
    private final MissingDependencyHandler missingDependencyHandler = new SimpMissingDependencyHandler();
    private static final int MAXIMUM_NUMBER_OF_ITERATIONS = 10000;

    public SimpClassInstantiationService(Set<ClassModel> classModels) {
        this.instantiationModels = classModels.stream()
                .sorted(Comparator.comparing(model -> model.getConstructor().getParameterCount()))
                .map(InstantiationModel::new)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    @Override
    public void instantiate() {
        logger.info("Instantiating classes \uD83D\uDD25");

        int iterations = 0;
        while (!this.instantiationModels.isEmpty()) {
            if (iterations > MAXIMUM_NUMBER_OF_ITERATIONS) break;
            InstantiationModel currentModel = instantiationModels.poll();

            dependencyResolverService.resolveConstructorDependencies(currentModel, this.availableInstances);
            if(currentModel.hasConstructorDependenciesResolved()){
                this.performConstructorInstantiation(currentModel);
            }

            dependencyResolverService.resolveAutowiredFieldDependencies(currentModel, this.availableInstances);
            if (currentModel.hasConstructorDependenciesResolved() && currentModel.hasAutowiredFieldsResolved()) {
                this.performAutowiredFieldAssignment(currentModel);
            }

            if (currentModel.isModelResolved()) {
                this.addAvailableInstance(currentModel);
                this.invokeAndSaveBeans(currentModel);
                this.invokePostConstructMethod(currentModel);
            } else {
                instantiationModels.add(currentModel);
            }

            iterations++;
        }

        if(instantiationModels.size() > 0){
            missingDependencyHandler.scanForMissingConstructorDependencies(this.instantiationModels);
            missingDependencyHandler.scanForMissingFieldDependencies(this.instantiationModels);
        }

        logger.info("All project classes were instantiated correctly ✅");
    }

    private void performConstructorInstantiation(InstantiationModel model) {
        try {
            if (model.getInstance() == null) {
                Constructor<?> classConstructor = model.getConstructor();
                Object[] params = model.getConstructorDependencyInstances();
                if (params.length == 0) {
                    Object instance = classConstructor.newInstance();
                    model.setInstance(instance);
                    return;
                }

                Object instance = classConstructor.newInstance(params);
                model.setInstance(instance);
            }
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void performAutowiredFieldAssignment(InstantiationModel model) {
        if (model.getAutowiredFields() == null) return;
        if (model.getAutowiredFields().length == 0) return;

        System.out.println(model.getInstance());
        for (int i = 0; i < model.getAutowiredFields().length; i++) {
            try {
                model.getAutowiredFields()[i].set(model.getInstance(),  model.getAutowiredFieldDependencyInstances()[i]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void addAvailableInstance(InstantiationModel model) {
        Class<?> type = model.getType();
        String instanceId = type.getName();

        if(type.isAnnotationPresent(Service.class)) {
            Service service = type.getAnnotation(Service.class);
            if(!service.id().equals("")) instanceId = service.id();
        }

        if(type.isAnnotationPresent(Component.class)) {
            Component component = type.getAnnotation(Component.class);
            if(!component.id().equals("")) instanceId = component.id();
        }

        if(type.isAnnotationPresent(Repository.class)) {
            Repository repository = type.getAnnotation(Repository.class);
            if(!repository.id().equals("")) instanceId = repository.id();
        }

        if (this.availableInstances.containsKey(instanceId)) {
            String errorMessage = String.format("""
                    Can not instantiate %s because id "%s" is already use by another component
                    """, model.getType(), instanceId);

            throw new DuplicatedIdentifierException(errorMessage);
        }

        this.availableInstances.put(instanceId, model.getInstance());
    }

    public void invokePostConstructMethod(InstantiationModel model) {
        try {
            Object instance = model.getInstance();
            Method postConstruct = model.getPostConstruct();

            if (postConstruct != null) {
                postConstruct.invoke(instance);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void invokeAndSaveBeans(InstantiationModel model) {
        Method[] beans = model.getBeans();
        Object instance = model.getInstance();

        try {
            for (Method method : beans) {
                Object beanInstance = method.invoke(instance);
                String beanId = method.getName();

                Bean bean = method.getAnnotation(Bean.class);
                if (!bean.id().equals("")) beanId = bean.id();

                boolean exists = availableInstances.containsKey(beanId);
                if (exists) {
                    throw new DuplicatedIdentifierException("There's already a bean with id " + beanId);
                }

                this.availableInstances.put(beanId, beanInstance);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<Object> getInstances() {
        return this.availableInstances.values();
    }
}