package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.CalendarioMensal;
import com.clareza.domain.model.DiaDoCalendario;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
public class RespostaCalendario {

    int mes;
    int ano;
    BigDecimal totalReceitas;
    BigDecimal totalDespesas;
    BigDecimal saldoDoMes;
    List<RespostaDia> dias;

    public static RespostaCalendario de(CalendarioMensal calendario, LocalDate hoje) {
        return RespostaCalendario.builder()
                .mes(calendario.getMes())
                .ano(calendario.getAno())
                .totalReceitas(calendario.getTotalReceitas())
                .totalDespesas(calendario.getTotalDespesas())
                .saldoDoMes(calendario.getSaldoDoMes())
                .dias(calendario.getDias().stream()
                        .map(dia -> RespostaDia.de(dia, hoje))
                        .collect(Collectors.toList()))
                .build();
    }

    @Value
    @Builder
    public static class RespostaDia {

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate data;

        BigDecimal totalReceitas;
        BigDecimal totalDespesas;
        BigDecimal saldoDoDia;
        List<RespostaTransacao> transacoes;

        static RespostaDia de(DiaDoCalendario dia, LocalDate hoje) {
            return RespostaDia.builder()
                    .data(dia.getData())
                    .totalReceitas(dia.getTotalReceitas())
                    .totalDespesas(dia.getTotalDespesas())
                    .saldoDoDia(dia.getSaldoDoDia())
                    .transacoes(dia.getTransacoes().stream()
                            .map(transacao -> RespostaTransacao.de(transacao, hoje))
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}
