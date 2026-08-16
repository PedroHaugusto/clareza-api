package com.clareza.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Value
public class Carteira {

    BigDecimal totalInvestido;
    BigDecimal rentabilidadeMediaPonderada;
    int quantidade;
    List<Investimento> investimentos;

    private Carteira(BigDecimal totalInvestido, BigDecimal rentabilidadeMediaPonderada,
                     List<Investimento> investimentos) {
        this.totalInvestido = totalInvestido;
        this.rentabilidadeMediaPonderada = rentabilidadeMediaPonderada;
        this.quantidade = investimentos.size();
        this.investimentos = Collections.unmodifiableList(investimentos);
    }

    public static Carteira de(List<Investimento> investimentos) {
        BigDecimal total = investimentos.stream()
                .map(Investimento::getValorInvestido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Carteira(total, ponderar(investimentos, total), investimentos);
    }

    private static BigDecimal ponderar(List<Investimento> investimentos, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal somaPonderada = BigDecimal.ZERO;
        for (Investimento investimento : investimentos) {
            somaPonderada = somaPonderada.add(
                    investimento.getValorInvestido().multiply(investimento.getRentabilidadeInformada()));
        }

        return somaPonderada.divide(total, 2, RoundingMode.HALF_UP);
    }
}
