package com.vpnservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VPN API")
                        .version("1.0")
                        .description("API for VPN service application"))
                .addSecurityItem(new SecurityRequirement().addList("JSESSIONID"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("JSESSIONID", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                        ));
    }
}
