package com.careforall.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration
 *
 * Configures Swagger/OpenAPI documentation for the Payment Service.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("HF Payment Service API")
                .description("Donation payment processing service with idempotency, state machine, and retry logic")
                .version("1.2")
                .contact(new Contact()
                    .name("API Avengers Team")
                    .email("team@hopefund.org")));
    }
}
