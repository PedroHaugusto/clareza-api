package com.clareza.application.port.in;

import com.clareza.domain.model.Usuario;

public interface ConsultarUsuarioLogadoUseCase {

    Usuario consultar(Long usuarioId);
}
