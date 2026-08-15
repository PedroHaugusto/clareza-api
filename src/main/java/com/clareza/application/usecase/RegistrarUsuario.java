package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeRegistro;
import com.clareza.application.port.in.RegistrarUsuarioUseCase;
import com.clareza.application.port.in.UsuarioAutenticado;
import com.clareza.application.port.out.CodificadorDeSenhaPort;
import com.clareza.application.port.out.GeradorDeTokenPort;
import com.clareza.application.port.out.UsuarioRepositoryPort;
import com.clareza.domain.exception.RegraDeNegocioException;
import com.clareza.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrarUsuario implements RegistrarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final CodificadorDeSenhaPort codificadorDeSenha;
    private final GeradorDeTokenPort geradorDeToken;

    @Override
    @Transactional
    public UsuarioAutenticado registrar(ComandoDeRegistro comando) {
        if (usuarioRepository.existePorEmail(comando.getEmail())) {
            throw new RegraDeNegocioException("Ja existe uma conta com este e-mail");
        }

        Usuario novoUsuario = Usuario.builder()
                .nome(comando.getNome())
                .email(comando.getEmail())
                .senhaHash(codificadorDeSenha.codificar(comando.getSenha()))
                .build();

        Usuario salvo = usuarioRepository.salvar(novoUsuario);
        GeradorDeTokenPort.TokenGerado token = geradorDeToken.gerarPara(salvo);

        return UsuarioAutenticado.builder()
                .id(salvo.getId())
                .nome(salvo.getNome())
                .email(salvo.getEmail())
                .token(token.getToken())
                .expiraEm(token.getExpiraEm())
                .build();
    }
}