package com.poms.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI pomsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Purchase Order Management System (POMS) REST API")
                        .description("REST API documentation for the Purchase Order Management System backend. "
                                + "Provides endpoints for authentication, dashboard statistics, vendor management, "
                                + "product catalog, purchase order lifecycle and approval workflows, inventory monitoring, "
                                + "and goods receipts tracking.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("POMS Engineering Team")
                                .email("nishanth@poms.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("http://localhost:5000")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT Bearer token obtained from POST /api/login.")));
    }
}
