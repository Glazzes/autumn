package com.glaze.autumn.application;

import com.glaze.autumn.instantiator.model.InstantiationQueuedModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface Runner {
    Logger logger = LogManager.getLogger(Runner.class);

    static void runApplication(InstantiationQueuedModel mainModel){
        Class<?> mainModelType = mainModel.getClassModel().getType();
        Object instance = mainModel.getInstance();

        logger.debug("Application started \uD83C\uDF1F");
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
