package com.sk.growthnav;

import com.sk.growthnav.api.conversation.repository.ConversationRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableJpaAuditing
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.sk.growthnav.api.conversation.repository")
@EnableJpaRepositories(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = {ConversationRepository.class}
))
@SpringBootApplication
public class GrowthNavApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrowthNavApplication.class, args);
    }

}
