package com.glaze.autumn.test;

import com.glaze.autumn.shared.annotation.Service;

@Service
public class ServiceFour {
    private final ServiceThree serviceThree;

    public ServiceFour(ServiceThree serviceThree){
        this.serviceThree = serviceThree;
    }
}
