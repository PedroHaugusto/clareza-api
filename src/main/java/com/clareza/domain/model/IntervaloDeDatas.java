package com.clareza.domain.model;

import lombok.Value;

import java.time.LocalDate;

@Value
public class IntervaloDeDatas {

    LocalDate inicio;
    LocalDate fim;

    public boolean contem(LocalDate data) {
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }
}
