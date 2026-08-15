package com.clareza.infrastructure.adapter.out.seguranca;

import com.clareza.application.port.out.ValidadorDeTokenGooglePort;
import com.clareza.domain.exception.AutenticacaoGoogleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class ValidadorDeTokenGoogle implements ValidadorDeTokenGooglePort {

    private static final List<String> EMISSORES_ACEITOS =
            Arrays.asList("https://accounts.google.com", "accounts.google.com");

    private static final String CLAIM_EMISSOR = "iss";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_EMAIL_VERIFICADO = "email_verified";
    private static final String CLAIM_NOME = "name";

    private final JwtDecoder decodificador;
    private final String clientId;

    public ValidadorDeTokenGoogle(@Qualifier("decodificadorDeTokenGoogle") JwtDecoder decodificador,
                                  @Value("${clareza.google.client-id}") String clientId) {
        this.decodificador = decodificador;
        this.clientId = clientId;
    }

    @Override
    public ContaGoogle validar(String idToken) {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new AutenticacaoGoogleException("Login com Google indisponivel: GOOGLE_CLIENT_ID nao configurado");
        }

        Jwt jwt = decodificar(idToken);
        conferirEmissor(jwt);
        conferirAudiencia(jwt);
        conferirEmailVerificado(jwt);

        String email = jwt.getClaimAsString(CLAIM_EMAIL);
        if (email == null || email.trim().isEmpty()) {
            throw new AutenticacaoGoogleException("A conta do Google nao informou um e-mail");
        }

        return new ContaGoogle(jwt.getSubject(), email, nomeOuParteDoEmail(jwt, email));
    }

    private Jwt decodificar(String idToken) {
        try {
            return decodificador.decode(idToken);
        } catch (JwtException excecao) {
            log.debug("Token do Google recusado: {}", excecao.getMessage());
            throw new AutenticacaoGoogleException("Nao foi possivel validar o login com o Google");
        }
    }

    private void conferirEmissor(Jwt jwt) {
        String emissor = jwt.getClaimAsString(CLAIM_EMISSOR);
        if (!EMISSORES_ACEITOS.contains(emissor)) {
            throw new AutenticacaoGoogleException("Nao foi possivel validar o login com o Google");
        }
    }

    private void conferirAudiencia(Jwt jwt) {
        List<String> audiencia = jwt.getAudience();
        if (audiencia == null || !audiencia.contains(clientId)) {
            throw new AutenticacaoGoogleException("Nao foi possivel validar o login com o Google");
        }
    }

    private void conferirEmailVerificado(Jwt jwt) {
        if (!Boolean.TRUE.equals(jwt.getClaim(CLAIM_EMAIL_VERIFICADO))) {
            throw new AutenticacaoGoogleException("A conta do Google precisa ter o e-mail verificado");
        }
    }

    private String nomeOuParteDoEmail(Jwt jwt, String email) {
        String nome = jwt.getClaimAsString(CLAIM_NOME);
        if (nome != null && !nome.trim().isEmpty()) {
            return nome;
        }
        return email.substring(0, email.indexOf('@')).toLowerCase(Locale.ROOT);
    }
}
