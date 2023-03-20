package com.glaze.autumn.formatters;

import java.util.*;

public class CircularDependencyExceptionFormatter {
    private final StringBuilder builder = new StringBuilder();

    public String getErrorMessage(Class<?> causedBy, Stack<Class<?>> currentBranch) {
        List<Class<?>> classes = new ArrayList<>(currentBranch);
        List<Class<?>> inBetweenClasses = this.getInBetweenClasses(causedBy, classes);

        builder.append("A circular dependency has been found among the following classes\n");
        builder.append("|¯¯¯¯¯¯|\n");

        this.appendClassErrorsToBuilder(inBetweenClasses);
        builder.append("|______|");

        return builder.toString();
    }

    private List<Class<?>> getInBetweenClasses(Class<?> conflictingClass, List<Class<?>> nodes) {
        boolean hasFoundConflictingClass = false;
        List<Class<?>> inBetweenClasses = new ArrayList<>();

        for(Class<?> clazz : nodes) {
            if(hasFoundConflictingClass) {
                inBetweenClasses.add(clazz);
            }

            if(!hasFoundConflictingClass && clazz.equals(conflictingClass)) {
                hasFoundConflictingClass = true;
                inBetweenClasses.add(clazz);
            }
        }

        return inBetweenClasses;
    }

    private void appendClassErrorsToBuilder(List<Class<?>> inBetweenClasses) {
        for(Class<?> clazz : inBetweenClasses) {
            String className = String.format("|   %s\n", clazz.getName());
            this.builder.append(className);
            this.builder.append("|      👇\n");
        }
    }

}
