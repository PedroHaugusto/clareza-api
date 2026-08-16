package com.clareza.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ConfiguracaoSeguranca {

    private static final String[] DOCUMENTACAO = {
            "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
    };

    private final FiltroDeAutenticacaoJwt filtroDeAutenticacaoJwt;
    private final PontoDeEntradaNaoAutorizado pontoDeEntradaNaoAutorizado;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain cadeiaDeFiltros(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint(pontoDeEntradaNaoAutorizado)
                .and()
                .authorizeRequests()
                // Rotas de entrada listadas uma a uma: com /api/auth/** qualquer rota nova
                // sob esse prefixo nasceria publica sem ninguem perceber.
                .antMatchers("/api/auth/registrar", "/api/auth/login", "/api/auth/google").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers(DOCUMENTACAO).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(filtroDeAutenticacaoJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
