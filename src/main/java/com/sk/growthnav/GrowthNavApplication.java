package com.sk.growthnav;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableJpaAuditing
@SpringBootApplication
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.sk.growthnav.api.conversation.repository")
public class GrowthNavApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrowthNavApplication.class, args);
    }

}
