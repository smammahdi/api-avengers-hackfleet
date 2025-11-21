package com.careforall.analytics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI analyticsServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Analytics Service API")
                        .description("CQRS Read Model Analytics Service for CareForAll Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CareForAll Team")
                                .email("support@careforall.com")));
    }
}
