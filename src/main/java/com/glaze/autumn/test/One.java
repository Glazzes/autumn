package com.glaze.autumn.test;

import com.glaze.autumn.annotations.Autowired;
import com.glaze.autumn.annotations.PostConstruct;
import com.glaze.autumn.annotations.Qualifier;
import com.glaze.autumn.annotations.Service;

@Service
public class One implements Inter{

    @Qualifier(id = "otherId")
    @Autowired
    private Four four;

    @PostConstruct
    public void init(){
        four.shout();
    }

}
