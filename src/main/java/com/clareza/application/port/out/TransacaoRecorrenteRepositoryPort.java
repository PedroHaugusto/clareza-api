package com.clareza.application.port.out;

import com.clareza.domain.model.TransacaoRecorrente;

import java.util.List;
import java.util.Optional;

public interface TransacaoRecorrenteRepositoryPort {

    List<TransacaoRecorrente> listarDoUsuario(Long usuarioId);

    Optional<TransacaoRecorrente> buscarPorId(Long id);

    TransacaoRecorrente salvar(TransacaoRecorrente recorrente);
}
