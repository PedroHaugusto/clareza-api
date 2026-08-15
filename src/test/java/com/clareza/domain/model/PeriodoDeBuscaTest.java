package com.clareza.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodoDeBuscaTest {

    private static final LocalDate MEIO_DE_AGOSTO = LocalDate.of(2026, 8, 15);

    @Test
    @DisplayName("TODOS nao restringe data alguma")
    void naoDeveGerarIntervalo_quandoOPeriodoETodos() {
        assertThat(PeriodoDeBusca.TODOS.intervaloA(MEIO_DE_AGOSTO)).isNull();
    }

    @Test
    @DisplayName("MES_ATUAL vai do primeiro ao ultimo dia do mes, incluindo o que ja passou")
    void deveCobrirOMesInteiro() {
        IntervaloDeDatas intervalo = PeriodoDeBusca.MES_ATUAL.intervaloA(MEIO_DE_AGOSTO);

        assertThat(intervalo.getInicio()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(intervalo.getFim()).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(intervalo.contem(LocalDate.of(2026, 8, 1))).isTrue();
        assertThat(intervalo.contem(LocalDate.of(2026, 8, 31))).isTrue();
        assertThat(intervalo.contem(LocalDate.of(2026, 9, 1))).isFalse();
    }

    @Test
    @DisplayName("MES_ATUAL respeita meses curtos")
    void deveRespeitarOUltimoDiaDeFevereiro() {
        IntervaloDeDatas intervalo = PeriodoDeBusca.MES_ATUAL.intervaloA(LocalDate.of(2027, 2, 10));

        assertThat(intervalo.getFim()).isEqualTo(LocalDate.of(2027, 2, 28));
    }

    @Test
    @DisplayName("proximos 30 e 90 dias comecam hoje e nao olham para tras")
    void deveContarAPartirDeHoje() {
        IntervaloDeDatas trinta = PeriodoDeBusca.PROXIMOS_30_DIAS.intervaloA(MEIO_DE_AGOSTO);
        IntervaloDeDatas noventa = PeriodoDeBusca.PROXIMOS_90_DIAS.intervaloA(MEIO_DE_AGOSTO);

        assertThat(trinta.getInicio()).isEqualTo(MEIO_DE_AGOSTO);
        assertThat(trinta.getFim()).isEqualTo(MEIO_DE_AGOSTO.plusDays(30));
        assertThat(trinta.contem(MEIO_DE_AGOSTO.minusDays(1))).isFalse();

        assertThat(noventa.getFim()).isEqualTo(MEIO_DE_AGOSTO.plusDays(90));
    }
}
