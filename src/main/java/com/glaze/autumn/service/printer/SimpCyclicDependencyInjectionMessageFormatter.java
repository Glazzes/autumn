package com.glaze.autumn.service.printer;

public class SimpCyclicDependencyInjectionMessageFormatter implements CyclicDependencyInjectionMessageFormatter {

    @Override
    public String formatMessage(Class<?>[] cyclicDependencies){
        StringBuilder errorMessage = new StringBuilder("""
                A cyclic dependency has been found among the following classes.\s
                 ______
                |      |
                """);

        for (int i = 0; i < cyclicDependencies.length; i++ ) {
            errorMessage.append(String.format("|    %s\n", cyclicDependencies[i].getTypeName()));
            if(i < cyclicDependencies.length - 1){
                errorMessage.append("|      |\n");
            }else{
                errorMessage.append("|______|\n");
            }
        }

        return errorMessage.toString();
    }

}
