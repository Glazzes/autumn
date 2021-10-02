package com.glaze.autumn.test;

import com.glaze.autumn.annotations.Autowired;
import com.glaze.autumn.annotations.Service;

@Service
public class Five {

    @Autowired
    private Four four;

}
