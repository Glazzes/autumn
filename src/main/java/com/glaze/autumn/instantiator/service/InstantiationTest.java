package com.glaze.autumn.instantiator.service;

import com.glaze.autumn.clscanner.model.ClassModel;
import com.glaze.autumn.dependencyresolver.service.DependencyResolverService;
import com.glaze.autumn.dependencyresolver.service.SimpDependencyResolverService;
import com.glaze.autumn.instantiator.exception.DuplicatedIdentifierException;
import com.glaze.autumn.instantiator.model.InstantiationModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class InstantiationTest implements ClassIntantiationServiceTwo {
    private final Queue<InstantiationModel> instantiationModels;
    private final Map<String, Object> availableInstances = new HashMap<>();
    private final DependencyResolverService dependencyResolverService = new SimpDependencyResolverService();
    private final int MAXIMUM_NUMBER_OF_ITERATIONS = 10000;

    public InstantiationTest(Set<ClassModel> classModels) {
        this.instantiationModels = classModels.stream()
                .sorted(Comparator.comparing(model -> model.getConstructor().getParameterCount()))
                .map(InstantiationModel::new)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    @Override
    public void instantiate() {
        int iterations = 0;
        while (!this.availableInstances.isEmpty()) {
            if (iterations > MAXIMUM_NUMBER_OF_ITERATIONS) break;
            InstantiationModel currentModel = instantiationModels.poll();

            dependencyResolverService.resolveConstructorDependencies(currentModel, this.availableInstances);
            this.performConstructorInstantiation(currentModel);

            dependencyResolverService.resolveAutowiredFieldDependencies(currentModel, this.availableInstances);
            if (currentModel.hasAutowiredFieldsResolved()) {
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
    }

    private void performConstructorInstantiation(InstantiationModel model) {
        try {
            if (model.hasConstructorDependenciesResolved() && model.getInstance() == null) {
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

        Object instance = model.getInstance();
        Field[] autowiredFields = model.getAutowiredFields();
        for (int i = 0; i < autowiredFields.length; i++) {
            Field autowiredField = autowiredFields[i];
            Object autowiredFieldDependency = model.getAutowiredFieldDependencyInstances()[i];
            try {
                autowiredField.set(instance, autowiredFieldDependency);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void addAvailableInstance(InstantiationModel model) {
        Object instance = model.getInstance();
        String instanceId = instance.getClass().getName();

        if (this.availableInstances.containsKey(instanceId)) {
            String errorMessage = String.format("""
                    Can not instantiate %s because id "%s" is already use by another component
                    """, model.getType(), instanceId);

            throw new DuplicatedIdentifierException(errorMessage);
        }

        this.availableInstances.put(instanceId, instance);
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
    public Map<String, Object> getInstances() {
        return null;
    }
}