package com.glaze.autumn.service.formatter;

import java.util.Set;

public class SimpCyclicDependencyInjectionMessageFormatter implements CyclicDependencyInjectionMessageFormatter {

    @Override
    public String getRootNodeErrorMessage(Class<?>[] circularDependencies){
        StringBuilder errorMessage = new StringBuilder("""
                A cyclic dependency has been found among the following classes.\s
                .______.
                |      |
                """);

        for (int i = 0; i < circularDependencies.length; i++ ) {
            errorMessage.append(String.format("|    %s\n", circularDependencies[i].getTypeName()));
            if(i < circularDependencies.length - 1){
                errorMessage.append("|      |\n");
            }else{
                errorMessage.append("|______|\n");
            }
        }

        return errorMessage.toString();
    }

    @Override
    public String getMessageOnSubNodesError(
            Class<?> rootClass,
            Class<?> causedBy,
            Set<Class<?>> circularDependencies
    ){
        System.out.println(rootClass);
        System.out.println(causedBy);
        System.out.println(circularDependencies);
        return null;
    }
}
