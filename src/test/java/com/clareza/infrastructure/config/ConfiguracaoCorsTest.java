package com.clareza.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracaoCorsTest {

    private static final String ORIGEM_LOCAL = "http://localhost:4200";
    private static final String ORIGEM_PRODUCAO = "https://clareza.vercel.app";

    private CorsConfigurationSource fonte;

    @BeforeEach
    void configurar() {
        ConfiguracaoCors configuracaoCors =
                new ConfiguracaoCors(Arrays.asList(ORIGEM_LOCAL, ORIGEM_PRODUCAO));
        fonte = configuracaoCors.corsConfigurationSource();
    }

    @Test
    @DisplayName("rota da api recebe as origens declaradas na configuracao")
    void deveLiberarAsOrigensDeclaradas_quandoARotaEDaApi() {
        CorsConfiguration configuracao = configuracaoPara("/api/transacoes");

        assertThat(configuracao).isNotNull();
        assertThat(configuracao.getAllowedOrigins())
                .containsExactly(ORIGEM_LOCAL, ORIGEM_PRODUCAO);
    }

    @Test
    @DisplayName("origem nao declarada nao e aceita")
    void naoDeveAceitarOrigemDesconhecida() {
        CorsConfiguration configuracao = configuracaoPara("/api/transacoes");

        assertThat(configuracao.checkOrigin("http://evil.com")).isNull();
        assertThat(configuracao.checkOrigin(ORIGEM_LOCAL)).isEqualTo(ORIGEM_LOCAL);
    }

    @Test
    @DisplayName("actuator fica fora do cors por nao ser consumido pelo navegador")
    void naoDeveAplicarCors_quandoARotaNaoEDaApi() {
        assertThat(configuracaoPara("/actuator/health")).isNull();
    }

    @Test
    @DisplayName("todos os verbos usados pelos endpoints planejados estao liberados")
    void deveLiberarOsVerbosUsadosPelaApi() {
        CorsConfiguration configuracao = configuracaoPara("/api/transacoes");

        assertThat(configuracao.getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Test
    @DisplayName("credenciais seguem desligadas porque o jwt viaja no header Authorization")
    void naoDevePermitirCredenciais() {
        CorsConfiguration configuracao = configuracaoPara("/api/transacoes");

        assertThat(configuracao.getAllowCredentials()).isNotEqualTo(Boolean.TRUE);
    }

    @Test
    @DisplayName("preflight fica em cache para nao repetir OPTIONS a cada chamada")
    void deveManterOPreflightEmCache() {
        CorsConfiguration configuracao = configuracaoPara("/api/transacoes");

        assertThat(configuracao.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("uma unica origem configurada continua sendo lida corretamente")
    void deveAceitarUmaUnicaOrigem() {
        List<String> apenasLocal = Arrays.asList(ORIGEM_LOCAL);
        CorsConfigurationSource fonteComUmaOrigem =
                new ConfiguracaoCors(apenasLocal).corsConfigurationSource();

        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/api/contas");
        CorsConfiguration configuracao = fonteComUmaOrigem.getCorsConfiguration(requisicao);

        assertThat(configuracao.getAllowedOrigins()).containsExactly(ORIGEM_LOCAL);
    }

    private CorsConfiguration configuracaoPara(String caminho) {
        return fonte.getCorsConfiguration(new MockHttpServletRequest("GET", caminho));
    }
}
