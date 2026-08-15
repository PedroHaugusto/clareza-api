package com.clareza.infrastructure.adapter.out.seguranca;

import com.clareza.application.port.out.GeradorDeTokenPort;
import com.clareza.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class ServicoDeTokenJwt implements GeradorDeTokenPort {

    private static final String CLAIM_NOME = "nome";
    private static final String CLAIM_EMAIL = "email";

    private final SecretKey chave;
    private final Duration expiracao;

    public ServicoDeTokenJwt(@Value("${clareza.jwt.segredo}") String segredo,
                             @Value("${clareza.jwt.expiracao-minutos}") long expiracaoEmMinutos) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracao = Duration.ofMinutes(expiracaoEmMinutos);
    }

    @Override
    public TokenGerado gerarPara(Usuario usuario) {
        Instant emissao = Instant.now();
        Instant vencimento = emissao.plus(expiracao);

        String token = Jwts.builder()
                .setSubject(String.valueOf(usuario.getId()))
                .claim(CLAIM_NOME, usuario.getNome())
                .claim(CLAIM_EMAIL, usuario.getEmail())
                .setIssuedAt(Date.from(emissao))
                .setExpiration(Date.from(vencimento))
                .signWith(chave, SignatureAlgorithm.HS256)
                .compact();

        return new TokenGerado(token, vencimento);
    }

    public Optional<Long> extrairUsuarioId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(chave)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.of(Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException excecao) {
            log.debug("Token recusado: {}", excecao.getMessage());
            return Optional.empty();
        }
    }
}
