package com.clareza.application.usecase;

import com.clareza.application.port.in.ConsultarUsuarioLogadoUseCase;
import com.clareza.application.port.out.UsuarioRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultarUsuarioLogado implements ConsultarUsuarioLogadoUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public Usuario consultar(Long usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
    }
}
