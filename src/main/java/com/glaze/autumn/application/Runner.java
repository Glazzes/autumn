package com.glaze.autumn.application;

import com.glaze.autumn.instantiator.model.InstantiationQueuedModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface Runner {

    static void runApplication(InstantiationQueuedModel mainModel){
        Class<?> mainModelType = mainModel.getClassModel().getType();
        Object instance = mainModel.getInstance();

        if(CommandLineRunner.class.isAssignableFrom(mainModelType)){
           try{
               Method runMethod = mainModelType.getMethod("run");
               if(runMethod.getReturnType().equals(void.class)
                 && runMethod.getParameterCount() == 0){
                   runMethod.invoke(instance);
               }
           }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
               e.printStackTrace();
           }
        }
    }

}
