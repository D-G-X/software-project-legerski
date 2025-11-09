package io.bootify.license_application_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI apiInfo() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Mallorca Holiday Rental Licensing API")
                                .description("API for managing applications, licenses, and payments for the Mallorca Holiday Rental Licensing system. Supports GDPR-compliant user management through Keycloak integration and ensures transparent, fair allocation of ETV licenses. ")
                                .version("1.0.0")
                )
                .components(
                        new Components()
                                .addSecuritySchemes("KeycloakOAuth2", new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                )
                )
                ;
    }
}
