package com.glaze.autumn.test;

import com.glaze.autumn.shared.annotation.Autowired;
import com.glaze.autumn.shared.annotation.Service;

@Service
public class ServiceFive {

    @Autowired
    public ServiceFive(EpicService serviceFour){

    }
}
