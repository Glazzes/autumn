package com.glaze.autumn.clscanner.service;

import com.glaze.autumn.clscanner.model.ClassModel;
import java.util.Set;

public interface ClassScannerService {
    Set<ClassModel> scanProjectClasses();
}
