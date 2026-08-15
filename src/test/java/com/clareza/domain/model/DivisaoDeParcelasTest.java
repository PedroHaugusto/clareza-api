package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DivisaoDeParcelasTest {

    @Test
    @DisplayName("divisao exata: 1200 em 3 vezes da 400 em cada")
    void deveDividirValorExato() {
        List<BigDecimal> parcelas = DivisaoDeParcelas.dividir(new BigDecimal("1200.00"), 3);

        assertThat(parcelas).containsExactly(
                new BigDecimal("400.00"), new BigDecimal("400.00"), new BigDecimal("400.00"));
    }

    @Test
    @DisplayName("a sobra de centavos vai para a ultima parcela")
    void deveAbsorverOsCentavosNaUltimaParcela() {
        List<BigDecimal> parcelas = DivisaoDeParcelas.dividir(new BigDecimal("100.00"), 3);

        assertThat(parcelas).containsExactly(
                new BigDecimal("33.33"), new BigDecimal("33.33"), new BigDecimal("33.34"));
    }

    @Test
    @DisplayName("a soma das parcelas sempre reconstroi o total, em qualquer divisao")
    void aSomaDasParcelasDeveFecharComOTotal() {
        String[] totais = {"100.00", "0.05", "999.99", "1234.56", "10.00", "77.77"};
        int[] quantidades = {2, 3, 4, 5, 6, 7, 12};

        for (String total : totais) {
            for (int quantidade : quantidades) {
                BigDecimal valorTotal = new BigDecimal(total);
                if (valorTotal.divide(BigDecimal.valueOf(quantidade), 2, java.math.RoundingMode.DOWN)
                        .compareTo(new BigDecimal("0.01")) < 0) {
                    continue;
                }

                List<BigDecimal> parcelas = DivisaoDeParcelas.dividir(valorTotal, quantidade);

                BigDecimal soma = parcelas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                assertThat(soma)
                        .as("total %s em %d parcelas", total, quantidade)
                        .isEqualByComparingTo(valorTotal);
                assertThat(parcelas).hasSize(quantidade);
            }
        }
    }

    @Test
    @DisplayName("toda parcela e positiva, inclusive quando a divisao e apertada")
    void todaParcelaDeveSerPositiva() {
        List<BigDecimal> parcelas = DivisaoDeParcelas.dividir(new BigDecimal("0.05"), 5);

        assertThat(parcelas).allSatisfy(parcela ->
                assertThat(parcela).isGreaterThan(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("valor baixo demais para a quantidade de parcelas e recusado")
    void deveRecusarValorQueNaoCobreAsParcelas() {
        assertThatThrownBy(() -> DivisaoDeParcelas.dividir(new BigDecimal("0.02"), 3))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("baixo demais");
    }

    @Test
    @DisplayName("parcelamento exige ao menos duas parcelas")
    void deveRecusarMenosDeDuasParcelas() {
        assertThatThrownBy(() -> DivisaoDeParcelas.dividir(new BigDecimal("100.00"), 1))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("pelo menos 2");
    }

    @Test
    @DisplayName("valor total precisa ser positivo")
    void deveRecusarValorNaoPositivo() {
        assertThatThrownBy(() -> DivisaoDeParcelas.dividir(BigDecimal.ZERO, 3))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> DivisaoDeParcelas.dividir(new BigDecimal("-10.00"), 3))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
