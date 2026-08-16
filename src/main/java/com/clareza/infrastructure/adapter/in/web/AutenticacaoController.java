package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.AutenticarComGoogleUseCase;
import com.clareza.application.port.in.AutenticarUsuarioUseCase;
import com.clareza.application.port.in.ComandoDeLogin;
import com.clareza.application.port.in.ComandoDeLoginComGoogle;
import com.clareza.application.port.in.ComandoDeRegistro;
import com.clareza.application.port.in.ConsultarUsuarioLogadoUseCase;
import com.clareza.application.port.in.RegistrarUsuarioUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeLogin;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeLoginComGoogle;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeRegistro;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaAutenticacao;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final RegistrarUsuarioUseCase registrarUsuario;
    private final AutenticarUsuarioUseCase autenticarUsuario;
    private final AutenticarComGoogleUseCase autenticarComGoogle;
    private final ConsultarUsuarioLogadoUseCase consultarUsuarioLogado;

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaAutenticacao registrar(@Valid @RequestBody RequisicaoDeRegistro requisicao) {
        ComandoDeRegistro comando = new ComandoDeRegistro(
                requisicao.getNome(), requisicao.getEmail(), requisicao.getSenha());
        return RespostaAutenticacao.de(registrarUsuario.registrar(comando));
    }

    @PostMapping("/login")
    public RespostaAutenticacao login(@Valid @RequestBody RequisicaoDeLogin requisicao) {
        ComandoDeLogin comando = new ComandoDeLogin(requisicao.getEmail(), requisicao.getSenha());
        return RespostaAutenticacao.de(autenticarUsuario.autenticar(comando));
    }

    @GetMapping("/eu")
    public RespostaUsuario consultarUsuarioLogado(@AuthenticationPrincipal Long usuarioId) {
        return RespostaUsuario.de(consultarUsuarioLogado.consultar(usuarioId));
    }

    @PostMapping("/google")
    public RespostaAutenticacao loginComGoogle(@Valid @RequestBody RequisicaoDeLoginComGoogle requisicao) {
        ComandoDeLoginComGoogle comando = new ComandoDeLoginComGoogle(requisicao.getIdToken());
        return RespostaAutenticacao.de(autenticarComGoogle.autenticar(comando));
    }
}
