package com.springbootmicroservices.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway - Packaging Microservices")
                        .version("1.0")
                        .description("Gateway for Order Packaging Microservice")
                        .contact(new Contact()
                                .name("L2 Teste")
                                .email("contact@email.com")));
    }
}
