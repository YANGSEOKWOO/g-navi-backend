package com.sk.growthnav.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("G.Navi API")
                        .description("4조 4toB팀 스웨거")
                        .version("v.1.0.0")
                        .contact(new Contact()
                                .name("4toB")));
    }
}