package com.clareza.infrastructure.adapter.out.seguranca;

import com.clareza.application.port.out.ValidadorDeTokenGooglePort;
import com.clareza.domain.exception.AutenticacaoGoogleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidadorDeTokenGoogleTest {

    private static final String CLIENT_ID = "123-app.apps.googleusercontent.com";
    private static final String OUTRO_CLIENT_ID = "999-outro.apps.googleusercontent.com";

    @Test
    @DisplayName("token legitimo devolve o identificador, o e-mail e o nome da conta")
    void deveExtrairOsDadosDaConta() {
        ValidadorDeTokenGoogle validador = validadorCom(jwtValido(mapaBase()));

        ValidadorDeTokenGooglePort.ContaGoogle conta = validador.validar("qualquer-token");

        assertThat(conta.getGoogleId()).isEqualTo("google-sub-123");
        assertThat(conta.getEmail()).isEqualTo("ana@clareza.dev");
        assertThat(conta.getNome()).isEqualTo("Ana Souza");
    }

    @Test
    @DisplayName("emissor sem o https tambem e aceito, porque o Google usa as duas formas")
    void deveAceitarOsDoisEmissoresDoGoogle() {
        Map<String, Object> claims = mapaBase();
        claims.put("iss", "accounts.google.com");

        ValidadorDeTokenGoogle validador = validadorCom(jwtValido(claims));

        assertThat(validador.validar("qualquer-token").getEmail()).isEqualTo("ana@clareza.dev");
    }

    @Test
    @DisplayName("token de outro emissor e recusado")
    void deveRecusarEmissorDesconhecido() {
        Map<String, Object> claims = mapaBase();
        claims.put("iss", "https://evil.com");

        assertThatThrownBy(() -> validadorCom(jwtValido(claims)).validar("qualquer-token"))
                .isInstanceOf(AutenticacaoGoogleException.class);
    }

    @Test
    @DisplayName("token emitido para outro aplicativo e recusado")
    void deveRecusarAudienciaDeOutroAplicativo() {
        Map<String, Object> claims = mapaBase();
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(600),
                cabecalho(), claims);
        ValidadorDeTokenGoogle validador =
                new ValidadorDeTokenGoogle(decodificadorQueDevolve(jwt), OUTRO_CLIENT_ID);

        assertThatThrownBy(() -> validador.validar("qualquer-token"))
                .isInstanceOf(AutenticacaoGoogleException.class);
    }

    @Test
    @DisplayName("e-mail nao verificado e recusado, para ninguem assumir a conta de outra pessoa")
    void deveRecusarEmailNaoVerificado() {
        Map<String, Object> claims = mapaBase();
        claims.put("email_verified", false);

        assertThatThrownBy(() -> validadorCom(jwtValido(claims)).validar("qualquer-token"))
                .isInstanceOf(AutenticacaoGoogleException.class)
                .hasMessageContaining("e-mail verificado");
    }

    @Test
    @DisplayName("assinatura invalida vira falha de autenticacao, e nao erro interno")
    void deveTraduzirFalhaDeAssinatura() {
        JwtDecoder decodificadorQueRecusa = token -> {
            throw new BadJwtException("assinatura invalida");
        };
        ValidadorDeTokenGoogle validador = new ValidadorDeTokenGoogle(decodificadorQueRecusa, CLIENT_ID);

        assertThatThrownBy(() -> validador.validar("token-falsificado"))
                .isInstanceOf(AutenticacaoGoogleException.class)
                .hasMessageContaining("Nao foi possivel validar o login com o Google");
    }

    @Test
    @DisplayName("sem GOOGLE_CLIENT_ID configurado o login social avisa em vez de aceitar qualquer token")
    void deveAvisar_quandoOClientIdNaoEstaConfigurado() {
        ValidadorDeTokenGoogle validador =
                new ValidadorDeTokenGoogle(decodificadorQueDevolve(jwtValido(mapaBase())), "  ");

        assertThatThrownBy(() -> validador.validar("qualquer-token"))
                .isInstanceOf(AutenticacaoGoogleException.class)
                .hasMessageContaining("GOOGLE_CLIENT_ID nao configurado");
    }

    @Test
    @DisplayName("conta sem nome usa a parte do e-mail antes do arroba")
    void deveUsarParteDoEmail_quandoNaoHaNome() {
        Map<String, Object> claims = mapaBase();
        claims.remove("name");

        assertThat(validadorCom(jwtValido(claims)).validar("qualquer-token").getNome())
                .isEqualTo("ana");
    }

    private ValidadorDeTokenGoogle validadorCom(Jwt jwt) {
        return new ValidadorDeTokenGoogle(decodificadorQueDevolve(jwt), CLIENT_ID);
    }

    private JwtDecoder decodificadorQueDevolve(Jwt jwt) {
        return token -> jwt;
    }

    private Jwt jwtValido(Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(600), cabecalho(), claims);
    }

    private Map<String, Object> cabecalho() {
        return Collections.singletonMap("alg", "RS256");
    }

    private Map<String, Object> mapaBase() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "https://accounts.google.com");
        claims.put("sub", "google-sub-123");
        claims.put("aud", Arrays.asList(CLIENT_ID));
        claims.put("email", "ana@clareza.dev");
        claims.put("email_verified", true);
        claims.put("name", "Ana Souza");
        return claims;
    }
}
