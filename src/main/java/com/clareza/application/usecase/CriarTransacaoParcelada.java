package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeParcelamento;
import com.clareza.application.port.in.CriarTransacaoParceladaUseCase;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.model.DivisaoDeParcelas;
import com.clareza.domain.model.Transacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriarTransacaoParcelada implements CriarTransacaoParceladaUseCase {

    private final TransacaoRepositoryPort transacaoRepository;
    private final VinculosDaTransacao vinculos;

    @Override
    @Transactional
    public List<Transacao> parcelar(ComandoDeParcelamento comando) {
        vinculos.exigirContaDoUsuario(comando.getContaId(), comando.getUsuarioId());
        vinculos.exigirCategoriaVisivel(comando.getCategoriaId(), comando.getUsuarioId());

        List<BigDecimal> valores =
                DivisaoDeParcelas.dividir(comando.getValorTotal(), comando.getTotalParcelas());

        UUID grupo = UUID.randomUUID();
        List<Transacao> parcelas = new ArrayList<>(valores.size());

        for (int indice = 0; indice < valores.size(); indice++) {
            parcelas.add(Transacao.builder()
                    .usuarioId(comando.getUsuarioId())
                    .contaId(comando.getContaId())
                    .categoriaId(comando.getCategoriaId())
                    .descricao(comando.getDescricao())
                    .valor(valores.get(indice))
                    .tipo(comando.getTipo())
                    .dataPrevista(comando.getDataDaPrimeiraParcela().plusMonths(indice))
                    .grupoParcelamentoId(grupo)
                    .numeroParcela(indice + 1)
                    .totalParcelas(comando.getTotalParcelas())
                    .build());
        }

        return transacaoRepository.salvarTodas(parcelas);
    }
}
