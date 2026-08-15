package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
public class TransacaoRecorrente {

    private final Long id;
    private final Long usuarioId;
    private final Long contaId;
    private final Long categoriaId;
    private final String descricao;
    private final BigDecimal valor;
    private final TipoTransacao tipo;
    private final Periodicidade periodicidade;
    private final Integer diaDoMes;
    private final Integer diaDaSemana;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final boolean ativa;

    @Builder(toBuilder = true)
    private TransacaoRecorrente(Long id, Long usuarioId, Long contaId, Long categoriaId,
                                String descricao, BigDecimal valor, TipoTransacao tipo,
                                Periodicidade periodicidade, Integer diaDoMes, Integer diaDaSemana,
                                LocalDate dataInicio, LocalDate dataFim, Boolean ativa) {
        exigir(usuarioId != null, "A recorrencia precisa pertencer a um usuario");
        exigir(contaId != null, "A conta e obrigatoria");
        exigir(categoriaId != null, "A categoria e obrigatoria");
        exigir(descricao != null && !descricao.trim().isEmpty(), "A descricao e obrigatoria");
        exigir(tipo != null, "O tipo e obrigatorio");
        exigir(periodicidade != null, "A periodicidade e obrigatoria");
        exigir(dataInicio != null, "A data de inicio e obrigatoria");
        exigir(valor != null && valor.compareTo(BigDecimal.ZERO) > 0,
                "O valor deve ser positivo: o sinal vem do tipo da transacao");
        exigir(dataFim == null || !dataFim.isBefore(dataInicio),
                "A data de fim nao pode ser anterior a data de inicio");

        if (periodicidade == Periodicidade.SEMANAL) {
            exigir(diaDaSemana != null, "A periodicidade semanal exige o dia da semana");
            exigir(diaDaSemana >= 1 && diaDaSemana <= 7, "O dia da semana vai de 1 a 7");
            exigir(diaDoMes == null, "A periodicidade semanal nao usa dia do mes");
        } else {
            exigir(diaDoMes != null, "Esta periodicidade exige o dia do mes");
            exigir(diaDoMes >= 1 && diaDoMes <= 31, "O dia do mes vai de 1 a 31");
            exigir(diaDaSemana == null, "Somente a periodicidade semanal usa dia da semana");
        }

        this.id = id;
        this.usuarioId = usuarioId;
        this.contaId = contaId;
        this.categoriaId = categoriaId;
        this.descricao = descricao.trim();
        this.valor = valor;
        this.tipo = tipo;
        this.periodicidade = periodicidade;
        this.diaDoMes = diaDoMes;
        this.diaDaSemana = diaDaSemana;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.ativa = ativa == null || ativa;
    }

    public List<LocalDate> ocorrenciasEntre(LocalDate hoje, LocalDate limiteDoHorizonte) {
        LocalDate inicioEfetivo = dataInicio.isBefore(hoje) ? hoje : dataInicio;
        LocalDate fimEfetivo = dataFim != null && dataFim.isBefore(limiteDoHorizonte)
                ? dataFim
                : limiteDoHorizonte;

        List<LocalDate> ocorrencias = new ArrayList<>();
        if (inicioEfetivo.isAfter(fimEfetivo)) {
            return ocorrencias;
        }

        LocalDate candidata = primeiraOcorrenciaAPartirDe(inicioEfetivo);
        while (!candidata.isAfter(fimEfetivo)) {
            ocorrencias.add(candidata);
            candidata = proximaDepoisDe(candidata);
        }
        return ocorrencias;
    }

    public TransacaoRecorrente desativar() {
        return toBuilder().ativa(false).build();
    }

    public boolean pertenceA(Long usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }

    private LocalDate primeiraOcorrenciaAPartirDe(LocalDate inicio) {
        if (periodicidade == Periodicidade.SEMANAL) {
            return inicio.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(diaDaSemana)));
        }

        LocalDate noMesDoInicio = noDiaDoMes(inicio);
        if (!noMesDoInicio.isBefore(inicio)) {
            return noMesDoInicio;
        }
        return periodicidade == Periodicidade.MENSAL
                ? noDiaDoMes(inicio.plusMonths(1))
                : noDiaDoMes(inicio.plusYears(1));
    }

    private LocalDate proximaDepoisDe(LocalDate atual) {
        switch (periodicidade) {
            case SEMANAL:
                return atual.plusWeeks(1);
            case MENSAL:
                return noDiaDoMes(atual.withDayOfMonth(1).plusMonths(1));
            default:
                return noDiaDoMes(atual.withDayOfMonth(1).plusYears(1));
        }
    }

    private LocalDate noDiaDoMes(LocalDate referencia) {
        int ultimoDia = referencia.lengthOfMonth();
        return referencia.withDayOfMonth(Math.min(diaDoMes, ultimoDia));
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraDeNegocioException(mensagem);
        }
    }
}
