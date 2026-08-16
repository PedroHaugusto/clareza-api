package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeMetaFinanceira;
import com.clareza.application.port.in.GerenciarMetasFinanceirasUseCase;
import com.clareza.application.port.out.MetaFinanceiraRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.model.MetaFinanceira;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GerenciarMetasFinanceiras implements GerenciarMetasFinanceirasUseCase {

    private final MetaFinanceiraRepositoryPort metaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MetaFinanceira> listar(Long usuarioId) {
        return metaRepository.listarDoUsuario(usuarioId);
    }

    @Override
    @Transactional
    public MetaFinanceira criar(ComandoDeMetaFinanceira comando) {
        return metaRepository.salvar(montar(null, comando));
    }

    @Override
    @Transactional
    public MetaFinanceira editar(Long metaId, ComandoDeMetaFinanceira comando) {
        MetaFinanceira existente = buscarDoUsuario(metaId, comando.getUsuarioId());
        return metaRepository.salvar(montar(existente.getId(), comando));
    }

    @Override
    @Transactional
    public void excluir(Long metaId, Long usuarioId) {
        MetaFinanceira meta = buscarDoUsuario(metaId, usuarioId);
        metaRepository.excluir(meta.getId());
    }

    private MetaFinanceira montar(Long id, ComandoDeMetaFinanceira comando) {
        return MetaFinanceira.builder()
                .id(id)
                .usuarioId(comando.getUsuarioId())
                .nome(comando.getNome())
                .valorAtual(comando.getValorAtual())
                .valorObjetivo(comando.getValorObjetivo())
                .prazo(comando.getPrazo())
                .descricao(comando.getDescricao())
                .build();
    }

    private MetaFinanceira buscarDoUsuario(Long metaId, Long usuarioId) {
        MetaFinanceira meta = metaRepository.buscarPorId(metaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Meta", metaId));

        if (!meta.pertenceA(usuarioId)) {
            throw new RecursoNaoEncontradoException("Meta", metaId);
        }
        return meta;
    }
}
