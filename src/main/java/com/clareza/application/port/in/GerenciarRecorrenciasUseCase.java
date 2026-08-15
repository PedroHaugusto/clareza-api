package com.clareza.application.port.in;

import com.clareza.domain.model.TransacaoRecorrente;

import java.util.List;

public interface GerenciarRecorrenciasUseCase {

    List<TransacaoRecorrente> listar(Long usuarioId);

    TransacaoRecorrente criar(ComandoDeRecorrencia comando);

    void desativar(Long recorrenteId, Long usuarioId);
}
