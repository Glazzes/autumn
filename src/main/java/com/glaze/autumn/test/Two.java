package com.glaze.autumn.test;

import com.glaze.autumn.annotations.Service;

@Service
public class Two {
    private final One one;

    public Two(One one){
        this.one = one;
    }
}
