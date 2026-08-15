package com.clareza.application.usecase;

import com.clareza.application.port.in.AutenticarUsuarioUseCase;
import com.clareza.application.port.in.ComandoDeLogin;
import com.clareza.application.port.in.UsuarioAutenticado;
import com.clareza.application.port.out.CodificadorDeSenhaPort;
import com.clareza.application.port.out.GeradorDeTokenPort;
import com.clareza.application.port.out.UsuarioRepositoryPort;
import com.clareza.domain.exception.CredenciaisInvalidasException;
import com.clareza.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutenticarUsuario implements AutenticarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final CodificadorDeSenhaPort codificadorDeSenha;
    private final GeradorDeTokenPort geradorDeToken;

    @Override
    @Transactional(readOnly = true)
    public UsuarioAutenticado autenticar(ComandoDeLogin comando) {
        Usuario usuario = usuarioRepository.buscarPorEmail(comando.getEmail())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!usuario.possuiSenha()) {
            throw new CredenciaisInvalidasException();
        }

        if (!codificadorDeSenha.confere(comando.getSenha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        GeradorDeTokenPort.TokenGerado token = geradorDeToken.gerarPara(usuario);

        return UsuarioAutenticado.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .token(token.getToken())
                .expiraEm(token.getExpiraEm())
                .build();
    }
}