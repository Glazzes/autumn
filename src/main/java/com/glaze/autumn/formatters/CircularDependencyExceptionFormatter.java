package com.glaze.autumn.formatters;

import java.util.*;

public class CircularDependencyExceptionFormatter {
    private final StringBuilder builder = new StringBuilder();

    public String getErrorMessage(Class<?> causedBy, Stack<Class<?>> currentBranch) {
        List<Class<?>> inBetweenClasses = this.getInBetweenClasses(causedBy, currentBranch.stream().toList());

        builder.append("""
                A circular dependency has been found among the following classes.\s
                |¯¯¯¯¯¯¯|
                """);

        this.addClassesToErrorMessage(inBetweenClasses);
        builder.append("""
        |_______|
        """);

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

    private void addClassesToErrorMessage(List<Class<?>> inBetweenClasses) {
        for(Class<?> clazz : inBetweenClasses) {
            String toAppend = String.format("""
            |   %s
            |      👇
            """, clazz.getName());
            builder.append(toAppend);
        }
    }

}
