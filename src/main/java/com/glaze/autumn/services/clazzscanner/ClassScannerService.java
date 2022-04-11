package com.glaze.autumn.services.clazzscanner;

import com.glaze.autumn.models.ClassModel;
import java.util.Set;

public interface ClassScannerService {
    Set<ClassModel> scanProjectClasses();
}
