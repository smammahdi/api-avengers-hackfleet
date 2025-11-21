package com.careforall.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration for API Gateway
 * Provides API documentation at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Gateway")
                .version("1.0")
                .description("Central API Gateway with JWT authentication and routing to microservices")
                .contact(new Contact()
                    .name("E-Commerce Platform Team")
                    .email("team@ecommerce.com")));
    }
}
