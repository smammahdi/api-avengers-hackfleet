package com.careforall.campaign.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration for Campaign Service
 * Provides API documentation at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campaignServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("HF Campaign Service API")
                .version("1.0")
                .description("Fundraising campaign management service for Care for All platform with category search, organizer management, and event publishing")
                .contact(new Contact()
                    .name("Care for All Platform Team")
                    .email("team@careforall.com")));
    }
}
