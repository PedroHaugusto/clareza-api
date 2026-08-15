package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class DivisaoDeParcelas {

    private static final int MINIMO_DE_PARCELAS = 2;
    private static final int MAXIMO_DE_PARCELAS = 480;
    private static final BigDecimal MENOR_VALOR_DE_PARCELA = new BigDecimal("0.01");

    private DivisaoDeParcelas() {
    }

    public static List<BigDecimal> dividir(BigDecimal valorTotal, int totalParcelas) {
        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("O valor total deve ser positivo");
        }
        if (totalParcelas < MINIMO_DE_PARCELAS) {
            throw new RegraDeNegocioException("Um parcelamento precisa de pelo menos 2 parcelas");
        }
        if (totalParcelas > MAXIMO_DE_PARCELAS) {
            throw new RegraDeNegocioException("Um parcelamento aceita no maximo 480 parcelas");
        }

        BigDecimal valorDaParcela = valorTotal.divide(
                BigDecimal.valueOf(totalParcelas), 2, RoundingMode.DOWN);

        if (valorDaParcela.compareTo(MENOR_VALOR_DE_PARCELA) < 0) {
            throw new RegraDeNegocioException(
                    "O valor total e baixo demais para ser dividido nesta quantidade de parcelas");
        }

        List<BigDecimal> parcelas = new ArrayList<>(totalParcelas);
        for (int i = 0; i < totalParcelas - 1; i++) {
            parcelas.add(valorDaParcela);
        }

        BigDecimal somaDasAnteriores = valorDaParcela.multiply(BigDecimal.valueOf(totalParcelas - 1L));
        parcelas.add(valorTotal.subtract(somaDasAnteriores));

        return parcelas;
    }
}
