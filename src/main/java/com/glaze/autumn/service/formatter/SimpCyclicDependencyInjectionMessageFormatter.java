package com.glaze.autumn.service.formatter;

import com.glaze.autumn.shared.constant.CDConstants;

import java.util.*;

public class SimpCyclicDependencyInjectionMessageFormatter implements CyclicDependencyInjectionMessageFormatter {

    @Override
    public String getRootNodeErrorMessage(Class<?>[] circularDependencies){
        StringBuilder errorBuilder = new StringBuilder("""
                A cyclic dependency has been found among the following classes.\s
                .______.
                |      |
                """);

        this.appendDependecyBox(errorBuilder, circularDependencies);
        return errorBuilder.toString();
    }

    @Override
    public String getMessageOnSubNodesError(
            Class<?> rootClass,
            Class<?> causedBy,
            Class<?>[] circularDependencies
    ){
        StringBuilder errorBuilder = new StringBuilder();
        errorBuilder.append(String.format("""
        A circular dependency was found when attempting to create an instance of %s.
        %s
            |
        """, rootClass, rootClass));

        Map<String, List<Class<?>>> prePostCD = this.getPreAndPostCDDependencies(causedBy, circularDependencies);

        for (Class<?> preCDDependency : prePostCD.get(CDConstants.PRECD)) {
            errorBuilder.append(String.format("%s\n", preCDDependency));
            errorBuilder.append("   |\n");
        }

        errorBuilder.append("""
        .______.
        |      |
        """);

        this.appendDependecyBox(errorBuilder, circularDependencies);
        return errorBuilder.toString();
    }

    public Map<String, List<Class<?>>> getPreAndPostCDDependencies(
            Class<?> causedBy,
            Class<?>[] circularDependencies
    ){
        Map<String, List<Class<?>>> prePostCD = new HashMap<>();
        prePostCD.put(CDConstants.PRECD, new ArrayList<>());
        prePostCD.put(CDConstants.POSTCD, new ArrayList<>());

        boolean hasBeenProblematicClassSpotted = false;
        for (Class<?> circularDependency : circularDependencies) {
            if(causedBy.equals(circularDependency)) hasBeenProblematicClassSpotted = true;

            if(hasBeenProblematicClassSpotted){
                prePostCD.get(CDConstants.POSTCD).add(circularDependency);
            }else{
                prePostCD.get(CDConstants.PRECD).add(circularDependency);
            }
        }

        return prePostCD;
    }

    private void appendDependecyBox(StringBuilder errorBuilder, Class<?>[] dependencies){
        for (int i = 0; i < dependencies.length; i++ ) {
            errorBuilder.append(String.format("|    %s\n", dependencies[i].getTypeName()));
            if(i < dependencies.length - 1){
                errorBuilder.append("|      |\n");
            }else{
                errorBuilder.append("|______|\n");
            }
        }
    }

}
