package com.clareza.domain.model;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public enum PeriodoDeBusca {

    TODOS {
        @Override
        public IntervaloDeDatas intervaloA(LocalDate hoje) {
            return null;
        }
    },

    MES_ATUAL {
        @Override
        public IntervaloDeDatas intervaloA(LocalDate hoje) {
            return new IntervaloDeDatas(
                    hoje.withDayOfMonth(1),
                    hoje.with(TemporalAdjusters.lastDayOfMonth()));
        }
    },

    PROXIMOS_30_DIAS {
        @Override
        public IntervaloDeDatas intervaloA(LocalDate hoje) {
            return new IntervaloDeDatas(hoje, hoje.plusDays(30));
        }
    },

    PROXIMOS_90_DIAS {
        @Override
        public IntervaloDeDatas intervaloA(LocalDate hoje) {
            return new IntervaloDeDatas(hoje, hoje.plusDays(90));
        }
    };

    public abstract IntervaloDeDatas intervaloA(LocalDate hoje);
}
