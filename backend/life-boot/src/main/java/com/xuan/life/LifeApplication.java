package com.xuan.life;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication(scanBasePackages = "com.xuan.life")
public class LifeApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(LifeApplication.class);
        application.setDefaultProperties(Map.of("spring.profiles.default", "dev"));
        application.run(args);
    }
}
