package com.clareza.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VisaoGeralTest {

    private static final YearMonth AGOSTO = YearMonth.of(2026, 8);

    @Test
    @DisplayName("saldo disponivel considera tudo ate o fim do mes atual, inclusive o previsto")
    void oSaldoDisponivelDeveIncluirOPrevistoDoMes() {
        List<TotalMensal> totais = Arrays.asList(
                total(AGOSTO, TipoTransacao.RECEITA, StatusTransacao.CONFIRMADA, "5000.00"),
                total(AGOSTO, TipoTransacao.DESPESA, StatusTransacao.CONFIRMADA, "1200.00"),
                total(AGOSTO, TipoTransacao.DESPESA, StatusTransacao.PREVISTA, "89.90"));

        VisaoGeral visaoGeral = VisaoGeral.montar(totais, AGOSTO, 3);

        assertThat(visaoGeral.getSaldoDisponivel()).isEqualByComparingTo("3710.10");
        assertThat(visaoGeral.getSaldoRealizado()).isEqualByComparingTo("3800.00");
    }

    @Test
    @DisplayName("lancamento de mes futuro nao mexe no saldo disponivel")
    void oMesSeguinteNaoDeveAfetarOSaldo() {
        List<TotalMensal> totais = Arrays.asList(
                total(AGOSTO, TipoTransacao.RECEITA, StatusTransacao.CONFIRMADA, "1000.00"),
                total(AGOSTO.plusMonths(1), TipoTransacao.DESPESA, StatusTransacao.PREVISTA, "800.00"),
                total(AGOSTO.plusMonths(1), TipoTransacao.RECEITA, StatusTransacao.PREVISTA, "5000.00"));

        VisaoGeral visaoGeral = VisaoGeral.montar(totais, AGOSTO, 3);

        assertThat(visaoGeral.getSaldoDisponivel()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("mes anterior continua contando para o saldo")
    void oMesAnteriorDeveContarNoSaldo() {
        List<TotalMensal> totais = Arrays.asList(
                total(AGOSTO.minusMonths(1), TipoTransacao.RECEITA, StatusTransacao.CONFIRMADA, "2000.00"),
                total(AGOSTO, TipoTransacao.DESPESA, StatusTransacao.CONFIRMADA, "500.00"));

        VisaoGeral visaoGeral = VisaoGeral.montar(totais, AGOSTO, 3);

        assertThat(visaoGeral.getSaldoDisponivel()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("o resumo do mes separa realizado de previsto")
    void deveSepararRealizadoDePrevistoNoMes() {
        List<TotalMensal> totais = Arrays.asList(
                total(AGOSTO, TipoTransacao.RECEITA, StatusTransacao.CONFIRMADA, "5000.00"),
                total(AGOSTO, TipoTransacao.RECEITA, StatusTransacao.PREVISTA, "800.00"),
                total(AGOSTO, TipoTransacao.DESPESA, StatusTransacao.CONFIRMADA, "1200.00"),
                total(AGOSTO, TipoTransacao.DESPESA, StatusTransacao.PREVISTA, "89.90"));

        ResumoDoMes mes = VisaoGeral.montar(totais, AGOSTO, 3).getMesAtual();

        assertThat(mes.getMes()).isEqualTo(8);
        assertThat(mes.getAno()).isEqualTo(2026);
        assertThat(mes.getReceitasRealizadas()).isEqualByComparingTo("5000.00");
        assertThat(mes.getReceitasPrevistas()).isEqualByComparingTo("800.00");
        assertThat(mes.getDespesasRealizadas()).isEqualByComparingTo("1200.00");
        assertThat(mes.getDespesasPrevistas()).isEqualByComparingTo("89.90");
        assertThat(mes.getTotalReceitas()).isEqualByComparingTo("5800.00");
        assertThat(mes.getTotalDespesas()).isEqualByComparingTo("1289.90");
        assertThat(mes.getSaldoDoMes()).isEqualByComparingTo("4510.10");
    }

    @Test
    @DisplayName("os proximos meses saem em sequencia, mesmo os sem lancamento")
    void deveProjetarOsProximosMesesEmSequencia() {
        List<TotalMensal> totais = Collections.singletonList(
                total(AGOSTO.plusMonths(2), TipoTransacao.DESPESA, StatusTransacao.PREVISTA, "300.00"));

        VisaoGeral visaoGeral = VisaoGeral.montar(totais, AGOSTO, 3);

        assertThat(visaoGeral.getProximosMeses()).hasSize(3);
        assertThat(visaoGeral.getProximosMeses()).extracting(ResumoDoMes::getMes)
                .containsExactly(9, 10, 11);
        assertThat(visaoGeral.getProximosMeses().get(0).getSaldoDoMes()).isEqualByComparingTo("0");
        assertThat(visaoGeral.getProximosMeses().get(1).getDespesasPrevistas())
                .isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("a projecao atravessa a virada do ano")
    void deveAtravessarAViradaDoAno() {
        VisaoGeral visaoGeral =
                VisaoGeral.montar(Collections.emptyList(), YearMonth.of(2026, 11), 3);

        assertThat(visaoGeral.getProximosMeses()).extracting(ResumoDoMes::getMes)
                .containsExactly(12, 1, 2);
        assertThat(visaoGeral.getProximosMeses()).extracting(ResumoDoMes::getAno)
                .containsExactly(2026, 2027, 2027);
    }

    @Test
    @DisplayName("usuario sem lancamento nenhum recebe zeros, e nao nulos")
    void deveDevolverZeradoQuandoNaoHaLancamentos() {
        VisaoGeral visaoGeral = VisaoGeral.montar(Collections.emptyList(), AGOSTO, 3);

        assertThat(visaoGeral.getSaldoDisponivel()).isEqualByComparingTo("0");
        assertThat(visaoGeral.getSaldoRealizado()).isEqualByComparingTo("0");
        assertThat(visaoGeral.getMesAtual().getTotalReceitas()).isEqualByComparingTo("0");
        assertThat(visaoGeral.getProximosMeses()).hasSize(3);
    }

    private TotalMensal total(YearMonth competencia, TipoTransacao tipo,
                              StatusTransacao status, String valor) {
        return new TotalMensal(competencia.getYear(), competencia.getMonthValue(),
                tipo, status, new BigDecimal(valor));
    }
}
