package com.clareza.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class ConfiguracaoLoginGoogle {

    private static final String CHAVES_PUBLICAS_DO_GOOGLE = "https://www.googleapis.com/oauth2/v3/certs";

    @Bean
    public JwtDecoder decodificadorDeTokenGoogle() {
        return NimbusJwtDecoder.withJwkSetUri(CHAVES_PUBLICAS_DO_GOOGLE).build();
    }
}
