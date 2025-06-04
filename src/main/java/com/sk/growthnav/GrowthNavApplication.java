package com.sk.growthnav;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GrowthNavApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrowthNavApplication.class, args);
    }

}
