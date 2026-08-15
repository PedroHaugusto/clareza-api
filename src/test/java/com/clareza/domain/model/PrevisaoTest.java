package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrevisaoTest {

    private static final YearMonth SETEMBRO = YearMonth.of(2026, 9);
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("1000.00");

    @Test
    @DisplayName("cenario provavel usa os valores registrados, sem ajuste")
    void oCenarioProvavelNaoDeveAjustar() {
        Previsao previsao = montar(Cenario.PROVAVEL, transacoesDeSetembro());

        PrevisaoMensal setembro = previsao.getMeses().get(0);
        assertThat(setembro.getTotalReceitasPrevistas()).isEqualByComparingTo("5000.00");
        assertThat(setembro.getTotalDespesasPrevistas()).isEqualByComparingTo("2000.00");
        assertThat(setembro.getSaldoProjetado()).isEqualByComparingTo("4000.00");
    }

    @Test
    @DisplayName("otimista sobe as receitas e derruba as despesas")
    void oCenarioOtimistaDeveMelhorarOsDoisLados() {
        Previsao previsao = montar(Cenario.OTIMISTA, transacoesDeSetembro());

        PrevisaoMensal setembro = previsao.getMeses().get(0);
        assertThat(setembro.getTotalReceitasPrevistas()).isEqualByComparingTo("5500.00");
        assertThat(setembro.getTotalDespesasPrevistas()).isEqualByComparingTo("1800.00");
        assertThat(setembro.getSaldoProjetado()).isEqualByComparingTo("4700.00");
    }

    @Test
    @DisplayName("pessimista derruba as receitas e sobe as despesas")
    void oCenarioPessimistaDevePiorarOsDoisLados() {
        Previsao previsao = montar(Cenario.PESSIMISTA, transacoesDeSetembro());

        PrevisaoMensal setembro = previsao.getMeses().get(0);
        assertThat(setembro.getTotalReceitasPrevistas()).isEqualByComparingTo("4500.00");
        assertThat(setembro.getTotalDespesasPrevistas()).isEqualByComparingTo("2200.00");
        assertThat(setembro.getSaldoProjetado()).isEqualByComparingTo("3300.00");
    }

    @Test
    @DisplayName("o saldo projetado de um mes vira o saldo inicial do seguinte")
    void deveEncadearOSaldoEntreOsMeses() {
        List<Transacao> transacoes = Arrays.asList(
                receita("Salario", "5000.00", SETEMBRO.atDay(5)),
                despesa("Aluguel", "2000.00", SETEMBRO.atDay(10)),
                receita("Salario", "5000.00", SETEMBRO.plusMonths(1).atDay(5)),
                despesa("Aluguel", "2000.00", SETEMBRO.plusMonths(1).atDay(10)));

        Previsao previsao = montar(Cenario.PROVAVEL, transacoes);

        PrevisaoMensal setembro = previsao.getMeses().get(0);
        PrevisaoMensal outubro = previsao.getMeses().get(1);

        assertThat(setembro.getSaldoInicial()).isEqualByComparingTo("1000.00");
        assertThat(setembro.getSaldoProjetado()).isEqualByComparingTo("4000.00");
        assertThat(outubro.getSaldoInicial()).isEqualByComparingTo("4000.00");
        assertThat(outubro.getSaldoProjetado()).isEqualByComparingTo("7000.00");
    }

    @Test
    @DisplayName("saldo projetado segue a formula do bloco 5")
    void deveAplicarAFormulaDoSaldoProjetado() {
        PrevisaoMensal mes = montar(Cenario.PROVAVEL, transacoesDeSetembro()).getMeses().get(0);

        BigDecimal esperado = mes.getSaldoInicial()
                .add(mes.getTotalReceitasPrevistas())
                .subtract(mes.getTotalDespesasPrevistas());

        assertThat(mes.getSaldoProjetado()).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("mes sem lancamento apenas carrega o saldo adiante")
    void mesVazioDeveManterOSaldo() {
        Previsao previsao = montar(Cenario.PROVAVEL, Collections.emptyList());

        assertThat(previsao.getMeses()).hasSize(6);
        assertThat(previsao.getMeses().get(0).getSaldoProjetado()).isEqualByComparingTo("1000.00");
        assertThat(previsao.getMeses().get(5).getSaldoInicial()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("horizonte de 12 meses atravessa a virada do ano")
    void deveProjetarDozeMeses() {
        Previsao previsao = Previsao.montar(SETEMBRO, 12, SALDO_INICIAL, Collections.emptyList(),
                Cenario.PROVAVEL, preferencia("10", "10"));

        assertThat(previsao.getMeses()).hasSize(12);
        assertThat(previsao.getMeses().get(0).getMes()).isEqualTo(9);
        assertThat(previsao.getMeses().get(0).getAno()).isEqualTo(2026);
        assertThat(previsao.getMeses().get(11).getMes()).isEqualTo(8);
        assertThat(previsao.getMeses().get(11).getAno()).isEqualTo(2027);
    }

    @Test
    @DisplayName("horizonte diferente de 6 ou 12 e recusado")
    void deveRecusarHorizonteInvalido() {
        assertThatThrownBy(() -> Previsao.montar(SETEMBRO, 3, SALDO_INICIAL,
                Collections.emptyList(), Cenario.PROVAVEL, preferencia("10", "10")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("6 ou 12");
    }

    @Test
    @DisplayName("percentuais diferentes para receita e despesa sao aplicados separadamente")
    void deveAplicarPercentuaisDistintos() {
        Previsao previsao = Previsao.montar(SETEMBRO, 6, SALDO_INICIAL, transacoesDeSetembro(),
                Cenario.PESSIMISTA, preferencia("20", "5"));

        PrevisaoMensal setembro = previsao.getMeses().get(0);
        assertThat(setembro.getTotalReceitasPrevistas()).isEqualByComparingTo("4000.00");
        assertThat(setembro.getTotalDespesasPrevistas()).isEqualByComparingTo("2100.00");
    }

    @Test
    @DisplayName("ajuste zero deixa qualquer cenario igual ao provavel")
    void ajusteZeroDeveNeutralizarOCenario() {
        Previsao otimista = Previsao.montar(SETEMBRO, 6, SALDO_INICIAL, transacoesDeSetembro(),
                Cenario.OTIMISTA, preferencia("0", "0"));

        assertThat(otimista.getMeses().get(0).getSaldoProjetado()).isEqualByComparingTo("4000.00");
    }

    @Test
    @DisplayName("as transacoes do mes acompanham a projecao, com o valor registrado")
    void deveDevolverAsTransacoesDoMes() {
        PrevisaoMensal setembro = montar(Cenario.PESSIMISTA, transacoesDeSetembro()).getMeses().get(0);

        assertThat(setembro.getTransacoes()).hasSize(2);
        assertThat(setembro.getTransacoes()).extracting(Transacao::getValor)
                .containsExactlyInAnyOrder(new BigDecimal("5000.00"), new BigDecimal("2000.00"));
    }

    private Previsao montar(Cenario cenario, List<Transacao> transacoes) {
        return Previsao.montar(SETEMBRO, 6, SALDO_INICIAL, transacoes, cenario,
                preferencia("10", "10"));
    }

    private List<Transacao> transacoesDeSetembro() {
        return Arrays.asList(
                receita("Salario", "5000.00", SETEMBRO.atDay(5)),
                despesa("Aluguel", "2000.00", SETEMBRO.atDay(10)));
    }

    private PreferenciaCenario preferencia(String receita, String despesa) {
        return PreferenciaCenario.builder()
                .usuarioId(1L)
                .percentualAjusteReceita(new BigDecimal(receita))
                .percentualAjusteDespesa(new BigDecimal(despesa))
                .build();
    }

    private Transacao receita(String descricao, String valor, LocalDate data) {
        return transacao(descricao, valor, data, TipoTransacao.RECEITA);
    }

    private Transacao despesa(String descricao, String valor, LocalDate data) {
        return transacao(descricao, valor, data, TipoTransacao.DESPESA);
    }

    private Transacao transacao(String descricao, String valor, LocalDate data, TipoTransacao tipo) {
        return Transacao.builder()
                .usuarioId(1L).contaId(10L).categoriaId(20L)
                .descricao(descricao).valor(new BigDecimal(valor)).tipo(tipo)
                .dataPrevista(data)
                .build();
    }
}
