package com.careforall.donation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration for Donation Service
 * Provides API documentation at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI donationServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("HF Donation Service API")
                .version("1.0.0")
                .description("Donation/Pledge service with Transactional Outbox pattern for reliable event publishing and eventual consistency")
                .contact(new Contact()
                    .name("CareForAll - Humanitarian Funding Platform")
                    .email("team@careforall.org")));
    }
}
