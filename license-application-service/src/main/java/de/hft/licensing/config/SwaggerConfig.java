package de.hft.licensing.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "KeycloakOAuth2";

    @Bean
    OpenAPI apiInfo() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Mallorca Holiday Rental Licensing API")
                                .description("API for managing applications, licenses, and payments for the Mallorca Holiday Rental Licensing system. Supports GDPR-compliant user management through Keycloak integration and ensures transparent, fair allocation of ETV licenses. ")
                                .version("v1")
                )
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .description("OAuth2 with Keycloak JWT Bearer tokens.")
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl("http://keycloak:8080/auth/realms/license-realm/protocol/openid-connect/auth")
                                                        .tokenUrl("http://keycloak:8080/auth/realms/license-realm/protocol/openid-connect/token")
                                                        .scopes(new Scopes()
                                                                .addString("openid", "OpenID Connect scope")
                                                                .addString("profile", "Profile information access")
                                                                .addString("email", "Email access"))))))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
