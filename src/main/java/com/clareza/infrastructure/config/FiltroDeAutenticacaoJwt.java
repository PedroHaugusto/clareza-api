package com.clareza.infrastructure.config;

import com.clareza.infrastructure.adapter.out.seguranca.ServicoDeTokenJwt;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FiltroDeAutenticacaoJwt extends OncePerRequestFilter {

    private static final String PREFIXO_BEARER = "Bearer ";

    private final ServicoDeTokenJwt servicoDeToken;

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao,
                                    HttpServletResponse resposta,
                                    FilterChain cadeia) throws ServletException, IOException {
        extrairToken(requisicao)
                .flatMap(servicoDeToken::extrairUsuarioId)
                .ifPresent(usuarioId -> autenticar(usuarioId, requisicao));

        cadeia.doFilter(requisicao, resposta);
    }

    private Optional<String> extrairToken(HttpServletRequest requisicao) {
        String cabecalho = requisicao.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecalho == null || !cabecalho.startsWith(PREFIXO_BEARER)) {
            return Optional.empty();
        }
        return Optional.of(cabecalho.substring(PREFIXO_BEARER.length()).trim());
    }

    private void autenticar(Long usuarioId, HttpServletRequest requisicao) {
        UsernamePasswordAuthenticationToken autenticacao =
                new UsernamePasswordAuthenticationToken(usuarioId, null, Collections.emptyList());
        autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(requisicao));
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }
}
