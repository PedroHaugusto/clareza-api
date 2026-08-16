package com.clareza.application.usecase;

import com.clareza.application.port.in.GerenciarMetaAporteUseCase;
import com.clareza.application.port.out.MetaAporteRepositoryPort;
import com.clareza.domain.model.MetaAporteMensal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GerenciarMetaAporte implements GerenciarMetaAporteUseCase {

    private final MetaAporteRepositoryPort metaAporteRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<MetaAporteMensal> consultar(Long usuarioId) {
        return metaAporteRepository.buscarDoUsuario(usuarioId);
    }

    @Override
    @Transactional
    public MetaAporteMensal definir(Long usuarioId, BigDecimal valor) {
        MetaAporteMensal meta = metaAporteRepository.buscarDoUsuario(usuarioId)
                .map(existente -> existente.toBuilder().valor(valor).build())
                .orElseGet(() -> MetaAporteMensal.builder().usuarioId(usuarioId).valor(valor).build());

        return metaAporteRepository.salvar(meta);
    }

    @Override
    @Transactional
    public void remover(Long usuarioId) {
        metaAporteRepository.excluirDoUsuario(usuarioId);
    }
}
