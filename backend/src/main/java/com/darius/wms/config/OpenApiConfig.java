package com.darius.wms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    /** Adds an "Authorize" button to Swagger UI: paste the token from POST /api/auth/login. */
    @Bean
    OpenAPI wmsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse Management System API")
                        .description("Products, storage locations, stock movements and order picking. "
                                + "Log in with POST /api/auth/login, then click Authorize and paste the token.")
                        .version("1.0"))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
