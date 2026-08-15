package com.clareza.infrastructure.adapter.out.seguranca;

import com.clareza.application.port.out.GeradorDeTokenPort;
import com.clareza.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ServicoDeTokenJwtTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-trinta-e-dois-bytes";
    private static final String OUTRO_SEGREDO = "outro-segredo-de-teste-com-mais-de-trinta-e-dois";

    private final ServicoDeTokenJwt servico = new ServicoDeTokenJwt(SEGREDO, 60);

    @Test
    @DisplayName("token emitido carrega o id do usuario e volta a ser lido")
    void deveGerarTokenLegivelPeloProprioServico() {
        GeradorDeTokenPort.TokenGerado gerado = servico.gerarPara(usuario());

        assertThat(gerado.getToken()).isNotBlank();
        assertThat(servico.extrairUsuarioId(gerado.getToken())).contains(42L);
    }

    @Test
    @DisplayName("vencimento respeita a expiracao configurada")
    void deveAplicarAExpiracaoConfigurada() {
        Instant antes = Instant.now();

        GeradorDeTokenPort.TokenGerado gerado = servico.gerarPara(usuario());

        assertThat(gerado.getExpiraEm()).isAfter(antes.plusSeconds(59 * 60L));
        assertThat(gerado.getExpiraEm()).isBefore(antes.plusSeconds(61 * 60L));
    }

    @Test
    @DisplayName("token assinado com outro segredo e recusado")
    void deveRecusarTokenDeOutroSegredo() {
        ServicoDeTokenJwt outroServico = new ServicoDeTokenJwt(OUTRO_SEGREDO, 60);
        String tokenIntruso = outroServico.gerarPara(usuario()).getToken();

        assertThat(servico.extrairUsuarioId(tokenIntruso)).isEmpty();
    }

    @Test
    @DisplayName("token expirado e recusado")
    void deveRecusarTokenExpirado() {
        ServicoDeTokenJwt servicoExpirado = new ServicoDeTokenJwt(SEGREDO, -1);
        String token = servicoExpirado.gerarPara(usuario()).getToken();

        assertThat(servico.extrairUsuarioId(token)).isEmpty();
    }

    @Test
    @DisplayName("texto que nao e um jwt e recusado sem estourar excecao")
    void deveRecusarTextoQueNaoEToken() {
        assertThat(servico.extrairUsuarioId("isso-nao-e-um-token")).isEqualTo(Optional.empty());
        assertThat(servico.extrairUsuarioId("")).isEmpty();
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(42L)
                .nome("Ana")
                .email("ana@clareza.dev")
                .senhaHash("hash-bcrypt")
                .build();
    }
}
