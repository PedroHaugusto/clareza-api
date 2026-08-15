package com.clareza.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class ConfiguracaoCors {

    private static final String PADRAO_DA_API = "/api/**";

    private static final List<String> METODOS_PERMITIDOS =
            Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    private static final long VALIDADE_DO_PREFLIGHT_EM_SEGUNDOS = 3600L;

    private final List<String> origensPermitidas;

    public ConfiguracaoCors(@Value("${clareza.cors.origens-permitidas}") List<String> origensPermitidas) {
        this.origensPermitidas = origensPermitidas;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(origensPermitidas);
        configuracao.setAllowedMethods(METODOS_PERMITIDOS);
        configuracao.setAllowedHeaders(Collections.singletonList(CorsConfiguration.ALL));
        configuracao.setAllowCredentials(false);
        configuracao.setMaxAge(VALIDADE_DO_PREFLIGHT_EM_SEGUNDOS);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration(PADRAO_DA_API, configuracao);
        return fonte;
    }
}