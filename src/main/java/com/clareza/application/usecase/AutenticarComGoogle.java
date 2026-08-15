package com.clareza.application.usecase;

import com.clareza.application.port.in.AutenticarComGoogleUseCase;
import com.clareza.application.port.in.ComandoDeLoginComGoogle;
import com.clareza.application.port.in.CriarContasPadraoUseCase;
import com.clareza.application.port.in.UsuarioAutenticado;
import com.clareza.application.port.out.GeradorDeTokenPort;
import com.clareza.application.port.out.UsuarioRepositoryPort;
import com.clareza.application.port.out.ValidadorDeTokenGooglePort;
import com.clareza.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutenticarComGoogle implements AutenticarComGoogleUseCase {

    private final ValidadorDeTokenGooglePort validadorDeTokenGoogle;
    private final UsuarioRepositoryPort usuarioRepository;
    private final GeradorDeTokenPort geradorDeToken;
    private final CriarContasPadraoUseCase criarContasPadrao;

    @Override
    @Transactional
    public UsuarioAutenticado autenticar(ComandoDeLoginComGoogle comando) {
        ValidadorDeTokenGooglePort.ContaGoogle conta =
                validadorDeTokenGoogle.validar(comando.getIdToken());

        Usuario usuario = usuarioRepository.buscarPorGoogleId(conta.getGoogleId())
                .orElseGet(() -> vincularOuCriar(conta));

        GeradorDeTokenPort.TokenGerado token = geradorDeToken.gerarPara(usuario);

        return UsuarioAutenticado.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .token(token.getToken())
                .expiraEm(token.getExpiraEm())
                .build();
    }

    private Usuario vincularOuCriar(ValidadorDeTokenGooglePort.ContaGoogle conta) {
        return usuarioRepository.buscarPorEmail(conta.getEmail())
                .map(existente -> usuarioRepository.salvar(existente.vincularGoogle(conta.getGoogleId())))
                .orElseGet(() -> criarNovoUsuario(conta));
    }

    private Usuario criarNovoUsuario(ValidadorDeTokenGooglePort.ContaGoogle conta) {
        Usuario salvo = usuarioRepository.salvar(Usuario.builder()
                .nome(conta.getNome())
                .email(conta.getEmail())
                .googleId(conta.getGoogleId())
                .build());
        criarContasPadrao.criarPara(salvo.getId());
        return salvo;
    }
}
