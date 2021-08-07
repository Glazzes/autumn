package com.glaze.autumn.test;

import com.glaze.autumn.shared.annotation.Autowired;
import com.glaze.autumn.shared.annotation.Component;
import com.glaze.autumn.shared.annotation.Service;

@Service
public class ServiceThree {

    @Autowired
    public ServiceThree(ServiceTwo epicService) {}
}
