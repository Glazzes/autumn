package com.glaze.autumn.services.instantiator;

import com.glaze.autumn.models.ClassModel;

import java.util.Comparator;

public class ClassModelInstatiatonComparator implements Comparator<ClassModel> {

    @Override
    public int compare(ClassModel o1, ClassModel o2) {
        return o1.getConstructor().getParameterCount() - o2.getConstructor().getParameterCount();
    }
}
