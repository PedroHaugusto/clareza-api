package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransacaoRecorrenteTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 8, 15);

    @Test
    @DisplayName("mensal gera uma ocorrencia por mes no dia escolhido")
    void deveGerarOcorrenciasMensais() {
        TransacaoRecorrente aluguel = mensal(10, LocalDate.of(2026, 8, 1), null);

        List<LocalDate> ocorrencias = aluguel.ocorrenciasEntre(HOJE, LocalDate.of(2026, 12, 31));

        assertThat(ocorrencias).containsExactly(
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 12, 10));
    }

    @Test
    @DisplayName("dia 31 cai no ultimo dia dos meses curtos, sem se perder nos seguintes")
    void deveAjustarODia31NosMesesCurtos() {
        TransacaoRecorrente assinatura = mensal(31, LocalDate.of(2027, 1, 1), null);

        List<LocalDate> ocorrencias =
                assinatura.ocorrenciasEntre(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 5, 31));

        assertThat(ocorrencias).containsExactly(
                LocalDate.of(2027, 1, 31),
                LocalDate.of(2027, 2, 28),
                LocalDate.of(2027, 3, 31),
                LocalDate.of(2027, 4, 30),
                LocalDate.of(2027, 5, 31));
    }

    @Test
    @DisplayName("fevereiro de ano bissexto recebe o dia 29")
    void deveUsarDia29EmAnoBissexto() {
        TransacaoRecorrente assinatura = mensal(31, LocalDate.of(2028, 2, 1), null);

        List<LocalDate> ocorrencias =
                assinatura.ocorrenciasEntre(LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29));

        assertThat(ocorrencias).containsExactly(LocalDate.of(2028, 2, 29));
    }

    @Test
    @DisplayName("semanal cai sempre no mesmo dia da semana")
    void deveGerarOcorrenciasSemanais() {
        TransacaoRecorrente feira = TransacaoRecorrente.builder()
                .usuarioId(1L).contaId(10L).categoriaId(20L)
                .descricao("Feira").valor(new BigDecimal("120.00")).tipo(TipoTransacao.DESPESA)
                .periodicidade(Periodicidade.SEMANAL)
                .diaDaSemana(6)
                .dataInicio(LocalDate.of(2026, 8, 1))
                .build();

        List<LocalDate> ocorrencias =
                feira.ocorrenciasEntre(HOJE, LocalDate.of(2026, 9, 10));

        assertThat(ocorrencias).containsExactly(
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 22),
                LocalDate.of(2026, 8, 29),
                LocalDate.of(2026, 9, 5));
        assertThat(ocorrencias).allMatch(data -> data.getDayOfWeek().getValue() == 6);
    }

    @Test
    @DisplayName("anual gera uma ocorrencia por ano")
    void deveGerarOcorrenciasAnuais() {
        TransacaoRecorrente ipva = TransacaoRecorrente.builder()
                .usuarioId(1L).contaId(10L).categoriaId(20L)
                .descricao("IPVA").valor(new BigDecimal("900.00")).tipo(TipoTransacao.DESPESA)
                .periodicidade(Periodicidade.ANUAL)
                .diaDoMes(20)
                .dataInicio(LocalDate.of(2027, 3, 1))
                .build();

        List<LocalDate> ocorrencias =
                ipva.ocorrenciasEntre(HOJE, LocalDate.of(2029, 12, 31));

        assertThat(ocorrencias).containsExactly(
                LocalDate.of(2027, 3, 20),
                LocalDate.of(2028, 3, 20),
                LocalDate.of(2029, 3, 20));
    }

    @Test
    @DisplayName("recorrencia antiga nao gera cobranca retroativa")
    void naoDeveGerarOcorrenciaNoPassado() {
        TransacaoRecorrente antiga = mensal(10, LocalDate.of(2020, 1, 1), null);

        List<LocalDate> ocorrencias = antiga.ocorrenciasEntre(HOJE, LocalDate.of(2026, 10, 31));

        assertThat(ocorrencias).containsExactly(
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 10, 10));
        assertThat(ocorrencias).allMatch(data -> !data.isBefore(HOJE));
    }

    @Test
    @DisplayName("data de fim encerra a serie antes do horizonte")
    void deveRespeitarADataDeFim() {
        TransacaoRecorrente comFim =
                mensal(10, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 10, 15));

        List<LocalDate> ocorrencias = comFim.ocorrenciasEntre(HOJE, LocalDate.of(2027, 8, 15));

        assertThat(ocorrencias).containsExactly(
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 10, 10));
    }

    @Test
    @DisplayName("recorrencia ja encerrada nao gera nada")
    void naoDeveGerarNada_quandoJaTerminou() {
        TransacaoRecorrente encerrada =
                mensal(10, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertThat(encerrada.ocorrenciasEntre(HOJE, LocalDate.of(2027, 8, 15))).isEmpty();
    }

    @Test
    @DisplayName("periodicidade semanal exige dia da semana, e nao dia do mes")
    void deveValidarOsCamposDaPeriodicidade() {
        assertThatThrownBy(() -> TransacaoRecorrente.builder()
                .usuarioId(1L).contaId(10L).categoriaId(20L)
                .descricao("Feira").valor(new BigDecimal("10.00")).tipo(TipoTransacao.DESPESA)
                .periodicidade(Periodicidade.SEMANAL)
                .dataInicio(HOJE)
                .build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("dia da semana");

        assertThatThrownBy(() -> TransacaoRecorrente.builder()
                .usuarioId(1L).contaId(10L).categoriaId(20L)
                .descricao("Aluguel").valor(new BigDecimal("10.00")).tipo(TipoTransacao.DESPESA)
                .periodicidade(Periodicidade.MENSAL)
                .dataInicio(HOJE)
                .build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("dia do mes");
    }

    @Test
    @DisplayName("data de fim anterior ao inicio e recusada")
    void deveRecusarDataDeFimAnteriorAoInicio() {
        assertThatThrownBy(() ->
                mensal(10, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 7, 1)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("data de fim");
    }

    @Test
    @DisplayName("nasce ativa e pode ser desativada")
    void deveNascerAtivaEPermitirDesativar() {
        TransacaoRecorrente ativa = mensal(10, HOJE, null);

        assertThat(ativa.isAtiva()).isTrue();
        assertThat(ativa.desativar().isAtiva()).isFalse();
    }

    private TransacaoRecorrente mensal(int diaDoMes, LocalDate inicio, LocalDate fim) {
        return TransacaoRecorrente.builder()
                .usuarioId(1L).contaId(10L).categoriaId(20L)
                .descricao("Aluguel").valor(new BigDecimal("1200.00")).tipo(TipoTransacao.DESPESA)
                .periodicidade(Periodicidade.MENSAL)
                .diaDoMes(diaDoMes)
                .dataInicio(inicio)
                .dataFim(fim)
                .build();
    }
}
