package com.glaze.autumn.services.scanner;

import com.glaze.autumn.models.ClassModel;
import java.util.Set;

public interface ClassScannerService {
    Set<ClassModel> getSuitableClasses();
}
