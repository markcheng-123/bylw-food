package com.bylw.foodforum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bylw.foodforum.mapper")
public class FoodForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodForumApplication.class, args);
    }
}
