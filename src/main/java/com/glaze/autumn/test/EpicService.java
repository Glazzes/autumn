package com.glaze.autumn.test;

import com.glaze.autumn.shared.annotations.Autowired;
import com.glaze.autumn.shared.annotations.Bean;
import com.glaze.autumn.shared.annotations.Component;
import com.glaze.autumn.shared.annotations.PostConstruct;

@Component
public class EpicService {
    @Autowired
    private final String autowiredString;

    public EpicService(String autowiredString) {
        this.autowiredString = autowiredString;
    }

    @PostConstruct
    public void init(){
        System.out.println("Hello i'm test service");
    }

    @Bean(id = "epicStringBean")
    public String someStringBean(){
        return "String bean";
    }

}
