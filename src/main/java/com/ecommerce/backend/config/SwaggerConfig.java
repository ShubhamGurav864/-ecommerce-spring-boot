package com.ecommerce.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        // 🔐 Security Scheme (JWT Bearer)
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()

                // 📄 API Info
                .info(new Info()
                        .title("Ecommerce Backend API")
                        .version("1.0")
                        .description("Spring Boot Ecommerce Backend with JWT Authentication")
                        .contact(new Contact()
                                .name("Shubham")
                                .email("your-email@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                )

                // 🔐 Add Security Globally
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))

                // 🔐 Register Security Scheme
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme));
    }
}