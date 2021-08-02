package com.glaze.autumn.test;

import com.glaze.autumn.shared.annotation.*;

@Repository(id = "Cool")
public class EpicService {

    @Autowired
    private final String autowiredString;

    public EpicService(ServiceFour serviceFour) {
        this.autowiredString = "";
    }

    @PostConstruct
    public void init(){
        System.out.println("Hello i'm test service");
    }

    @Bean(id = "epicStringBean")
    public String someStringBean(){
        return "String bean";
    }

    @Bean
    public Integer someIntegerBean(){
        return 100;
    }

}
