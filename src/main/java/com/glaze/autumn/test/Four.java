package com.glaze.autumn.test;

import com.glaze.autumn.annotations.Service;

@Service(id = "otherId")
public class Four {

    public void shout(){
        System.out.println("Im four!!!!");
    }

}
