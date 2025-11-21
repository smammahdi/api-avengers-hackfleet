package com.careforall.banking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration for Banking Service
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HopeFund Banking Service API")
                        .description("Banking service that simulates payment authorization and capture")
                        .version("2.0")
                        .contact(new Contact()
                                .name("API Avengers Team")
                                .email("support@hopefund.com")));
    }
}
