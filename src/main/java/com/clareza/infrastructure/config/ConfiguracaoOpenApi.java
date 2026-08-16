package com.clareza.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoOpenApi {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI documentacaoDaApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Clareza API")
                        .version("v1")
                        .description("API de organizacao financeira pessoal. Todas as rotas exigem "
                                + "JWT no cabecalho Authorization, exceto /api/auth/registrar, "
                                + "/api/auth/login, /api/auth/google e /actuator/health. "
                                + "Use o botao Authorize com o token devolvido no login."))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
