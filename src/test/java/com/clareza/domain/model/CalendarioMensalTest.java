package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalendarioMensalTest {

    @Test
    @DisplayName("lancamentos do mesmo dia sao agrupados e somados por tipo")
    void deveAgruparPorDiaESomarPorTipo() {
        List<Transacao> transacoes = Arrays.asList(
                despesa("Aluguel", "1200.00", LocalDate.of(2026, 8, 10)),
                despesa("Mercado", "89.90", LocalDate.of(2026, 8, 10)),
                receita("Salario", "5000.00", LocalDate.of(2026, 8, 5)));

        CalendarioMensal calendario = CalendarioMensal.montar(8, 2026, transacoes);

        assertThat(calendario.getDias()).hasSize(2);

        DiaDoCalendario diaCinco = calendario.getDias().get(0);
        assertThat(diaCinco.getData()).isEqualTo(LocalDate.of(2026, 8, 5));
        assertThat(diaCinco.getTotalReceitas()).isEqualByComparingTo("5000.00");
        assertThat(diaCinco.getTotalDespesas()).isEqualByComparingTo("0.00");

        DiaDoCalendario diaDez = calendario.getDias().get(1);
        assertThat(diaDez.getTotalDespesas()).isEqualByComparingTo("1289.90");
        assertThat(diaDez.getTransacoes()).hasSize(2);
        assertThat(diaDez.getSaldoDoDia()).isEqualByComparingTo("-1289.90");
    }

    @Test
    @DisplayName("os dias saem em ordem cronologica, independente da ordem de entrada")
    void deveOrdenarOsDias() {
        List<Transacao> transacoes = Arrays.asList(
                despesa("Terceiro", "10.00", LocalDate.of(2026, 8, 28)),
                despesa("Primeiro", "10.00", LocalDate.of(2026, 8, 3)),
                despesa("Segundo", "10.00", LocalDate.of(2026, 8, 15)));

        CalendarioMensal calendario = CalendarioMensal.montar(8, 2026, transacoes);

        assertThat(calendario.getDias()).extracting(DiaDoCalendario::getData)
                .containsExactly(
                        LocalDate.of(2026, 8, 3),
                        LocalDate.of(2026, 8, 15),
                        LocalDate.of(2026, 8, 28));
    }

    @Test
    @DisplayName("totais do mes somam todos os dias")
    void deveSomarOsTotaisDoMes() {
        List<Transacao> transacoes = Arrays.asList(
                receita("Salario", "5000.00", LocalDate.of(2026, 8, 5)),
                receita("Freela", "800.00", LocalDate.of(2026, 8, 20)),
                despesa("Aluguel", "1200.00", LocalDate.of(2026, 8, 10)));

        CalendarioMensal calendario = CalendarioMensal.montar(8, 2026, transacoes);

        assertThat(calendario.getTotalReceitas()).isEqualByComparingTo("5800.00");
        assertThat(calendario.getTotalDespesas()).isEqualByComparingTo("1200.00");
        assertThat(calendario.getSaldoDoMes()).isEqualByComparingTo("4600.00");
    }

    @Test
    @DisplayName("lancamento de outro mes e descartado")
    void deveIgnorarTransacaoForaDoMes() {
        List<Transacao> transacoes = Arrays.asList(
                despesa("Do mes", "10.00", LocalDate.of(2026, 8, 10)),
                despesa("Mes seguinte", "99.00", LocalDate.of(2026, 9, 1)),
                despesa("Mesmo mes outro ano", "99.00", LocalDate.of(2025, 8, 10)));

        CalendarioMensal calendario = CalendarioMensal.montar(8, 2026, transacoes);

        assertThat(calendario.getDias()).hasSize(1);
        assertThat(calendario.getTotalDespesas()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("mes sem lancamento devolve calendario zerado, e nao nulo")
    void deveDevolverCalendarioVazio() {
        CalendarioMensal calendario = CalendarioMensal.montar(8, 2026, Collections.emptyList());

        assertThat(calendario.getDias()).isEmpty();
        assertThat(calendario.getTotalReceitas()).isEqualByComparingTo("0");
        assertThat(calendario.getSaldoDoMes()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("mes fora da faixa e recusado")
    void deveRecusarMesInvalido() {
        assertThatThrownBy(() -> CalendarioMensal.montar(13, 2026, Collections.emptyList()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("entre 1 e 12");
    }

    private Transacao despesa(String descricao, String valor, LocalDate data) {
        return transacao(descricao, valor, data, TipoTransacao.DESPESA);
    }

    private Transacao receita(String descricao, String valor, LocalDate data) {
        return transacao(descricao, valor, data, TipoTransacao.RECEITA);
    }

    private Transacao transacao(String descricao, String valor, LocalDate data, TipoTransacao tipo) {
        return Transacao.builder()
                .usuarioId(1L).contaId(10L).categoriaId(20L)
                .descricao(descricao).valor(new BigDecimal(valor)).tipo(tipo)
                .dataPrevista(data)
                .build();
    }
}
