package com.clareza.application.usecase;

import com.clareza.application.port.in.ConsultarFluxoDeCaixaUseCase;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.exception.RegraDeNegocioException;
import com.clareza.domain.model.FluxoDeCaixa;
import com.clareza.domain.model.TotalMensal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultarFluxoDeCaixa implements ConsultarFluxoDeCaixaUseCase {

    static final int MAXIMO_DE_MESES = 24;

    private final TransacaoRepositoryPort transacaoRepository;

    @Override
    @Transactional(readOnly = true)
    public FluxoDeCaixa consultar(Long usuarioId, int mesesPassados, int mesesFuturos) {
        exigirFaixaValida(mesesPassados, "mesesPassados");
        exigirFaixaValida(mesesFuturos, "mesesFuturos");

        YearMonth competenciaAtual = YearMonth.from(LocalDate.now());
        YearMonth primeira = competenciaAtual.minusMonths(mesesPassados);
        YearMonth ultima = competenciaAtual.plusMonths(mesesFuturos);

        List<TotalMensal> totais =
                transacaoRepository.totalizarPorMesAte(usuarioId, ultima.atEndOfMonth());

        return FluxoDeCaixa.montar(totais, primeira, ultima);
    }

    private void exigirFaixaValida(int meses, String campo) {
        if (meses < 0 || meses > MAXIMO_DE_MESES) {
            throw new RegraDeNegocioException(
                    String.format("O campo %s deve estar entre 0 e %d", campo, MAXIMO_DE_MESES));
        }
    }
}
