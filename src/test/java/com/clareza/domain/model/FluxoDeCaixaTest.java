package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FluxoDeCaixaTest {

    private static final YearMonth JUNHO = YearMonth.of(2026, 6);

    @Test
    @DisplayName("o saldo acumulado avanca de um mes para o outro")
    void deveAcumularOSaldoAoLongoDosMeses() {
        List<TotalMensal> totais = Arrays.asList(
                total(JUNHO, TipoTransacao.RECEITA, "5000.00"),
                total(JUNHO, TipoTransacao.DESPESA, "4200.00"),
                total(JUNHO.plusMonths(1), TipoTransacao.RECEITA, "5000.00"),
                total(JUNHO.plusMonths(1), TipoTransacao.DESPESA, "3100.00"));

        FluxoDeCaixa fluxo = FluxoDeCaixa.montar(totais, JUNHO, JUNHO.plusMonths(1));

        assertThat(fluxo.getMeses()).hasSize(2);

        FluxoMensal junho = fluxo.getMeses().get(0);
        assertThat(junho.getEntradas()).isEqualByComparingTo("5000.00");
        assertThat(junho.getSaidas()).isEqualByComparingTo("4200.00");
        assertThat(junho.getSaldoDoMes()).isEqualByComparingTo("800.00");
        assertThat(junho.getSaldoAcumulado()).isEqualByComparingTo("800.00");

        FluxoMensal julho = fluxo.getMeses().get(1);
        assertThat(julho.getSaldoDoMes()).isEqualByComparingTo("1900.00");
        assertThat(julho.getSaldoAcumulado()).isEqualByComparingTo("2700.00");
    }

    @Test
    @DisplayName("o que aconteceu antes da janela entra como saldo anterior")
    void deveComecarDoSaldoAnteriorAJanela() {
        List<TotalMensal> totais = Arrays.asList(
                total(JUNHO.minusMonths(3), TipoTransacao.RECEITA, "10000.00"),
                total(JUNHO.minusMonths(2), TipoTransacao.DESPESA, "2500.00"),
                total(JUNHO, TipoTransacao.RECEITA, "1000.00"));

        FluxoDeCaixa fluxo = FluxoDeCaixa.montar(totais, JUNHO, JUNHO);

        assertThat(fluxo.getSaldoAnterior()).isEqualByComparingTo("7500.00");
        assertThat(fluxo.getMeses().get(0).getSaldoAcumulado()).isEqualByComparingTo("8500.00");
        assertThat(fluxo.getMeses().get(0).getSaldoDoMes()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("mes deficitario derruba o acumulado")
    void deveReduzirOAcumuladoNoMesDeficitario() {
        List<TotalMensal> totais = Arrays.asList(
                total(JUNHO, TipoTransacao.RECEITA, "1000.00"),
                total(JUNHO.plusMonths(1), TipoTransacao.DESPESA, "1500.00"));

        FluxoDeCaixa fluxo = FluxoDeCaixa.montar(totais, JUNHO, JUNHO.plusMonths(1));

        assertThat(fluxo.getMeses().get(1).getSaldoDoMes()).isEqualByComparingTo("-1500.00");
        assertThat(fluxo.getMeses().get(1).getSaldoAcumulado()).isEqualByComparingTo("-500.00");
    }

    @Test
    @DisplayName("meses sem movimento aparecem zerados, carregando o acumulado")
    void mesesSemMovimentoDevemAparecer() {
        List<TotalMensal> totais = Collections.singletonList(
                total(JUNHO, TipoTransacao.RECEITA, "1000.00"));

        FluxoDeCaixa fluxo = FluxoDeCaixa.montar(totais, JUNHO, JUNHO.plusMonths(2));

        assertThat(fluxo.getMeses()).hasSize(3);
        assertThat(fluxo.getMeses().get(1).getEntradas()).isEqualByComparingTo("0");
        assertThat(fluxo.getMeses().get(1).getSaldoAcumulado()).isEqualByComparingTo("1000.00");
        assertThat(fluxo.getMeses().get(2).getSaldoAcumulado()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("a janela atravessa a virada do ano")
    void deveAtravessarAViradaDoAno() {
        FluxoDeCaixa fluxo = FluxoDeCaixa.montar(
                Collections.emptyList(), YearMonth.of(2026, 11), YearMonth.of(2027, 2));

        assertThat(fluxo.getMeses()).hasSize(4);
        assertThat(fluxo.getMeses()).extracting(FluxoMensal::getMes).containsExactly(11, 12, 1, 2);
        assertThat(fluxo.getMeses()).extracting(FluxoMensal::getAno)
                .containsExactly(2026, 2026, 2027, 2027);
    }

    @Test
    @DisplayName("lancamento posterior a janela nao entra em mes algum")
    void deveIgnorarOQueEstaAlemDaJanela() {
        List<TotalMensal> totais = Arrays.asList(
                total(JUNHO, TipoTransacao.RECEITA, "1000.00"),
                total(JUNHO.plusMonths(5), TipoTransacao.RECEITA, "9999.00"));

        FluxoDeCaixa fluxo = FluxoDeCaixa.montar(totais, JUNHO, JUNHO.plusMonths(1));

        assertThat(fluxo.getMeses().get(1).getSaldoAcumulado()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("janela de um mes so e valida")
    void deveAceitarJanelaDeUmMes() {
        FluxoDeCaixa fluxo = FluxoDeCaixa.montar(Collections.emptyList(), JUNHO, JUNHO);

        assertThat(fluxo.getMeses()).hasSize(1);
    }

    @Test
    @DisplayName("fim anterior ao inicio e recusado")
    void deveRecusarJanelaInvertida() {
        assertThatThrownBy(() -> FluxoDeCaixa.montar(
                Collections.emptyList(), JUNHO, JUNHO.minusMonths(1)))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    private TotalMensal total(YearMonth competencia, TipoTransacao tipo, String valor) {
        return new TotalMensal(competencia.getYear(), competencia.getMonthValue(),
                tipo, StatusTransacao.CONFIRMADA, new BigDecimal(valor));
    }
}
