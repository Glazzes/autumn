package com.glaze.autumn.test;

import com.glaze.autumn.annotations.Autowired;
import com.glaze.autumn.annotations.Qualifier;
import com.glaze.autumn.annotations.Service;

@Service
public class One {

    @Qualifier(id = "something")
    @Autowired
    private Four four;

    @Autowired
    public One(){}

    public One(Three three){
        // this.three = three;
    }


}
