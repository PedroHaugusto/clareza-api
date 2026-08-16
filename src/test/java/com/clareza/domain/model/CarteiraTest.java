package com.clareza.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class CarteiraTest {

    @Test
    @DisplayName("a rentabilidade e ponderada pelo valor, nao uma media simples")
    void deveaPonderarPeloValorInvestido() {
        Carteira carteira = Carteira.de(Arrays.asList(
                investimento("CDB", "50000.00", "11.00", TipoInvestimento.RENDA_FIXA),
                investimento("Bitcoin", "1000.00", "25.00", TipoInvestimento.CRIPTO)));

        assertThat(carteira.getTotalInvestido()).isEqualByComparingTo("51000.00");
        assertThat(carteira.getRentabilidadeMediaPonderada()).isEqualByComparingTo("11.27");
        assertThat(carteira.getQuantidade()).isEqualTo(2);
    }

    @Test
    @DisplayName("com valores iguais a ponderada coincide com a media simples")
    void comValoresIguaisDeveIgualarAMediaSimples() {
        Carteira carteira = Carteira.de(Arrays.asList(
                investimento("A", "1000.00", "10.00", TipoInvestimento.RENDA_FIXA),
                investimento("B", "1000.00", "20.00", TipoInvestimento.ACOES)));

        assertThat(carteira.getRentabilidadeMediaPonderada()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("rentabilidade negativa puxa a media para baixo")
    void deveAceitarRentabilidadeNegativa() {
        Carteira carteira = Carteira.de(Arrays.asList(
                investimento("Acao boa", "1000.00", "20.00", TipoInvestimento.ACOES),
                investimento("Acao ruim", "1000.00", "-40.00", TipoInvestimento.ACOES)));

        assertThat(carteira.getRentabilidadeMediaPonderada()).isEqualByComparingTo("-10.00");
    }

    @Test
    @DisplayName("carteira vazia devolve zeros, sem divisao por zero")
    void carteiraVaziaDeveDevolverZeros() {
        Carteira carteira = Carteira.de(Collections.emptyList());

        assertThat(carteira.getTotalInvestido()).isEqualByComparingTo("0");
        assertThat(carteira.getRentabilidadeMediaPonderada()).isEqualByComparingTo("0");
        assertThat(carteira.getQuantidade()).isZero();
        assertThat(carteira.getInvestimentos()).isEmpty();
    }

    @Test
    @DisplayName("um unico investimento define a rentabilidade da carteira")
    void umInvestimentoDeveDefinirARentabilidade() {
        Carteira carteira = Carteira.de(Collections.singletonList(
                investimento("Tesouro", "300.00", "13.75", TipoInvestimento.TESOURO)));

        assertThat(carteira.getRentabilidadeMediaPonderada()).isEqualByComparingTo("13.75");
    }

    private Investimento investimento(String nome, String valor, String rentabilidade,
                                      TipoInvestimento tipo) {
        return Investimento.builder()
                .usuarioId(1L)
                .nome(nome)
                .tipo(tipo)
                .valorInvestido(new BigDecimal(valor))
                .rentabilidadeInformada(new BigDecimal(rentabilidade))
                .build();
    }
}
