package com.glaze.autumn.test;

import com.glaze.autumn.shared.annotation.Service;


@Service(id = "Some super epic service")
public class ServiceTwo {
    private final EpicService epicService;

    public ServiceTwo(EpicService epicService){
        this.epicService = epicService;
    }

}
