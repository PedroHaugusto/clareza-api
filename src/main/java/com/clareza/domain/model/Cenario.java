package com.clareza.domain.model;

import java.math.BigDecimal;

public enum Cenario {

    PROVAVEL {
        @Override
        public BigDecimal fatorParaReceitas(BigDecimal percentualAjuste) {
            return BigDecimal.ONE;
        }

        @Override
        public BigDecimal fatorParaDespesas(BigDecimal percentualAjuste) {
            return BigDecimal.ONE;
        }
    },

    OTIMISTA {
        @Override
        public BigDecimal fatorParaReceitas(BigDecimal percentualAjuste) {
            return BigDecimal.ONE.add(comoFracao(percentualAjuste));
        }

        @Override
        public BigDecimal fatorParaDespesas(BigDecimal percentualAjuste) {
            return BigDecimal.ONE.subtract(comoFracao(percentualAjuste));
        }
    },

    PESSIMISTA {
        @Override
        public BigDecimal fatorParaReceitas(BigDecimal percentualAjuste) {
            return BigDecimal.ONE.subtract(comoFracao(percentualAjuste));
        }

        @Override
        public BigDecimal fatorParaDespesas(BigDecimal percentualAjuste) {
            return BigDecimal.ONE.add(comoFracao(percentualAjuste));
        }
    };

    public abstract BigDecimal fatorParaReceitas(BigDecimal percentualAjuste);

    public abstract BigDecimal fatorParaDespesas(BigDecimal percentualAjuste);

    static BigDecimal comoFracao(BigDecimal percentual) {
        return percentual.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
    }
}
