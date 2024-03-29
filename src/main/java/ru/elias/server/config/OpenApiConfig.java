package ru.elias.server.config;

import java.util.List;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.elias.server.util.OpenApiConstants;

@SecuritySchemes(
        value = {
                @SecurityScheme(
                        name = OpenApiConstants.OAUTH2,
                        description = OpenApiConstants.OAUTH2_DESCRIPTION,
                        type = SecuritySchemeType.OAUTH2,
                        flows = @OAuthFlows(
                                authorizationCode = @OAuthFlow(
                                        authorizationUrl = OpenApiConstants.OAUTH2_AUTHORIZATION_URL,
                                        tokenUrl = OpenApiConstants.OAUTH2_TOKEN_URL,
                                        scopes = {
                                                @OAuthScope(name = OpenApiConstants.OAUTH2_OPENID_SCOPE),
                                                @OAuthScope(name = OpenApiConstants.OAUTH2_EMAIL_SCOPE),
                                                @OAuthScope(name = OpenApiConstants.OAUTH2_PROFILE_SCOPE),
                                        }
                                )
                        ),
                        extensions = @Extension(
                                properties = @ExtensionProperty(name = "x-tokenName", value = "id_token")
                        )
                ),
                @SecurityScheme(
                        name = OpenApiConstants.JWT,
                        description = OpenApiConstants.JWT_DESCRIPTION,
                        type = SecuritySchemeType.APIKEY,
                        in = SecuritySchemeIn.HEADER
                )
        }
)
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .paths(new Paths().addPathItem("/", new PathItem()))
                .info(new Info().title(OpenApiConstants.TITLE))
                .security(
                        List.of(
                                new SecurityRequirement().addList(OpenApiConstants.OAUTH2),
                                new SecurityRequirement().addList(OpenApiConstants.JWT)
                        )
                );
    }

}
