package com.darius.wms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI wmsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Warehouse Management System API")
                .description("Products, storage locations, stock movements and order picking")
                .version("1.0"));
    }
}
