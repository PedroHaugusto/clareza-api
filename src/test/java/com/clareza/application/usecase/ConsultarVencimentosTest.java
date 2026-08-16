package com.clareza.application.usecase;

import com.clareza.application.port.out.TransacaoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarVencimentosTest {

    @Mock
    private TransacaoRepositoryPort transacaoRepository;

    @Test
    @DisplayName("a janela de 14 dias parte do dia do usuario, nao do dia do servidor em UTC")
    void aJanelaDeveUsarOFusoDaAplicacao() {
        Clock noBrasilAs21h = Clock.fixed(
                Instant.parse("2026-08-16T00:40:00Z"), ZoneId.of("America/Sao_Paulo"));
        ConsultarVencimentos consultarVencimentos =
                new ConsultarVencimentos(transacaoRepository, noBrasilAs21h);
        when(transacaoRepository.listarPrevistasAte(eq(1L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        consultarVencimentos.consultar(1L);

        ArgumentCaptor<LocalDate> limite = ArgumentCaptor.forClass(LocalDate.class);
        verify(transacaoRepository).listarPrevistasAte(eq(1L), limite.capture());

        assertThat(limite.getValue()).isEqualTo(LocalDate.of(2026, 8, 29));
    }
}
