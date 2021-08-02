package com.glaze.autumn.service.scanner.clsscanner;

import com.glaze.autumn.model.ClassModel;
import java.util.Set;

public interface ClassScannerService {
    Set<ClassModel> getSuitableClasses();
}
