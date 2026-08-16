package com.clareza.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracaoDataHoraTest {

    private final ConfiguracaoDataHora configuracao = new ConfiguracaoDataHora();

    @Test
    @DisplayName("o relogio usa o fuso configurado, nao o do servidor")
    void deveUsarOFusoConfigurado() {
        Clock relogio = configuracao.relogio("America/Sao_Paulo");

        assertThat(relogio.getZone()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
    }

    @Test
    @DisplayName("as 21h no Brasil o dia ainda e hoje, mesmo o servidor em UTC ja estando na madrugada seguinte")
    void naoDeveVirarODiaAntesDaMeiaNoiteNoBrasil() {
        Instant vinteEUmaHoraDeQuinzeDeAgosto = Instant.parse("2026-08-16T00:40:00Z");

        LocalDate emSaoPaulo = LocalDate.now(
                Clock.fixed(vinteEUmaHoraDeQuinzeDeAgosto, ZoneId.of("America/Sao_Paulo")));
        LocalDate emUtc = LocalDate.now(
                Clock.fixed(vinteEUmaHoraDeQuinzeDeAgosto, ZoneOffset.UTC));

        assertThat(emSaoPaulo).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(emUtc).isEqualTo(LocalDate.of(2026, 8, 16));
    }

    @Test
    @DisplayName("logo apos a meia-noite no Brasil o dia ja virou")
    void deveVirarODiaNaMeiaNoiteLocal() {
        Clock relogio = Clock.fixed(
                Instant.parse("2026-08-16T03:10:00Z"), ZoneId.of("America/Sao_Paulo"));

        assertThat(LocalDate.now(relogio)).isEqualTo(LocalDate.of(2026, 8, 16));
    }

    @Test
    @DisplayName("fuso desconhecido falha no boot, e nao silenciosamente em producao")
    void deveFalharComFusoInvalido() {
        try {
            configuracao.relogio("Marte/Olympus");
            throw new AssertionError("deveria ter recusado o fuso invalido");
        } catch (java.time.zone.ZoneRulesException esperado) {
            assertThat(esperado).hasMessageContaining("Marte/Olympus");
        }
    }
}
