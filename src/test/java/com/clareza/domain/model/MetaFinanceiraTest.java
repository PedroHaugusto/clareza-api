package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetaFinanceiraTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 8, 16);

    @Test
    @DisplayName("percentual e restante saem do valor atual sobre o objetivo")
    void deveCalcularOsDerivados() {
        MetaFinanceira meta = meta("2500.00", "10000.00", null);

        assertThat(meta.getPercentualConcluido()).isEqualByComparingTo("25.00");
        assertThat(meta.getValorRestante()).isEqualByComparingTo("7500.00");
        assertThat(meta.estaConcluida()).isFalse();
    }

    @Test
    @DisplayName("meta superada mostra o percentual real, mas o restante para em zero")
    void metaSuperadaDeveTerRestanteZerado() {
        MetaFinanceira meta = meta("12000.00", "10000.00", null);

        assertThat(meta.getPercentualConcluido()).isEqualByComparingTo("120.00");
        assertThat(meta.getValorRestante()).isEqualByComparingTo("0.00");
        assertThat(meta.estaConcluida()).isTrue();
    }

    @Test
    @DisplayName("valor exatamente igual ao objetivo conclui a meta")
    void valorExatoDeveConcluir() {
        MetaFinanceira meta = meta("10000.00", "10000.00", null);

        assertThat(meta.getPercentualConcluido()).isEqualByComparingTo("100.00");
        assertThat(meta.getValorRestante()).isEqualByComparingTo("0.00");
        assertThat(meta.estaConcluida()).isTrue();
    }

    @Test
    @DisplayName("meta nova, sem nada guardado, comeca em zero por cento")
    void metaNovaDeveComecarEmZero() {
        MetaFinanceira meta = MetaFinanceira.builder()
                .usuarioId(1L).nome("Viagem").valorObjetivo(new BigDecimal("5000.00")).build();

        assertThat(meta.getValorAtual()).isEqualByComparingTo("0");
        assertThat(meta.getPercentualConcluido()).isEqualByComparingTo("0.00");
        assertThat(meta.getValorRestante()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("dias ate o prazo sao contados a partir de hoje")
    void deveContarOsDiasAtePrazo() {
        MetaFinanceira comPrazo = meta("100.00", "1000.00", HOJE.plusDays(30));

        assertThat(comPrazo.diasAte(HOJE)).isEqualTo(30L);
        assertThat(comPrazo.prazoVencidoEm(HOJE)).isFalse();
    }

    @Test
    @DisplayName("prazo passado com meta aberta conta como vencido, com dias negativos")
    void prazoPassadoDeveIndicarAtraso() {
        MetaFinanceira vencida = meta("100.00", "1000.00", HOJE.minusDays(5));

        assertThat(vencida.diasAte(HOJE)).isEqualTo(-5L);
        assertThat(vencida.prazoVencidoEm(HOJE)).isTrue();
    }

    @Test
    @DisplayName("meta concluida nao aparece como vencida, mesmo com o prazo passado")
    void metaConcluidaNaoDeveAparecerComoVencida() {
        MetaFinanceira concluida = meta("1000.00", "1000.00", HOJE.minusDays(5));

        assertThat(concluida.prazoVencidoEm(HOJE)).isFalse();
    }

    @Test
    @DisplayName("sem prazo nao ha dias nem atraso")
    void semPrazoNaoDeveDerivarNada() {
        MetaFinanceira semPrazo = meta("100.00", "1000.00", null);

        assertThat(semPrazo.diasAte(HOJE)).isNull();
        assertThat(semPrazo.prazoVencidoEm(HOJE)).isFalse();
    }

    @Test
    @DisplayName("objetivo precisa ser positivo e o valor atual nao pode ser negativo")
    void deveValidarOsValores() {
        assertThatThrownBy(() -> meta("100.00", "0", null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("objetivo");

        assertThatThrownBy(() -> meta("-1.00", "1000.00", null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("negativo");
    }

    @Test
    @DisplayName("descricao em branco vira nula em vez de string vazia")
    void descricaoEmBrancoDeveVirarNula() {
        MetaFinanceira meta = MetaFinanceira.builder()
                .usuarioId(1L).nome("Viagem").valorObjetivo(new BigDecimal("100.00"))
                .descricao("   ").build();

        assertThat(meta.getDescricao()).isNull();
    }

    private MetaFinanceira meta(String atual, String objetivo, LocalDate prazo) {
        return MetaFinanceira.builder()
                .usuarioId(1L)
                .nome("Viagem")
                .valorAtual(new BigDecimal(atual))
                .valorObjetivo(new BigDecimal(objetivo))
                .prazo(prazo)
                .build();
    }
}
