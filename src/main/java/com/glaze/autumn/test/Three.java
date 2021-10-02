package com.glaze.autumn.test;

import com.glaze.autumn.annotations.Service;

@Service
public class Three {

    private final Two two;
    public Three(Two two){
        this.two = two;
    }

}
