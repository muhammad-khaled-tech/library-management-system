package com.mkhaled.library.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI libraryOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Library Management System API")
                .version("v1")
                .description("Manage authors, books and members, and lend books with due dates and late fees."));
    }
}
