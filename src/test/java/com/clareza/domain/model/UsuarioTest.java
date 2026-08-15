package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

    private static final String HASH = "$2a$10$abcdefghijklmnopqrstuv";

    @Test
    @DisplayName("email e guardado em minusculas e sem espacos nas pontas")
    void deveNormalizarOEmail_quandoVemComEspacosEMaiusculas() {
        Usuario usuario = Usuario.builder()
                .nome("Ana")
                .email("  Ana.Souza@Clareza.DEV  ")
                .senhaHash(HASH)
                .build();

        assertThat(usuario.getEmail()).isEqualTo("ana.souza@clareza.dev");
    }

    @Test
    @DisplayName("nome tambem perde os espacos nas pontas")
    void deveRemoverEspacosDoNome() {
        Usuario usuario = Usuario.builder()
                .nome("  Ana Souza  ")
                .email("ana@clareza.dev")
                .senhaHash(HASH)
                .build();

        assertThat(usuario.getNome()).isEqualTo("Ana Souza");
    }

    @Test
    @DisplayName("conta sem senha e sem google nunca conseguiria autenticar")
    void deveRecusarUsuario_quandoNaoTemSenhaNemGoogle() {
        assertThatThrownBy(() -> Usuario.builder()
                .nome("Ana")
                .email("ana@clareza.dev")
                .build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("senha ou de um vinculo com o Google");
    }

    @Test
    @DisplayName("so com google e valido, sem senha")
    void deveAceitarUsuario_quandoTemApenasGoogle() {
        Usuario usuario = Usuario.builder()
                .nome("Ana")
                .email("ana@clareza.dev")
                .googleId("google-123")
                .build();

        assertThat(usuario.possuiSenha()).isFalse();
        assertThat(usuario.vinculadoAoGoogle()).isTrue();
    }

    @Test
    @DisplayName("nome vazio e recusado")
    void deveRecusarUsuario_quandoONomeEVazio() {
        assertThatThrownBy(() -> Usuario.builder()
                .nome("   ")
                .email("ana@clareza.dev")
                .senhaHash(HASH)
                .build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("nome");
    }

    @Test
    @DisplayName("vincular o google preserva a senha, para os dois logins continuarem valendo")
    void deveManterASenha_quandoVinculaOGoogle() {
        Usuario comSenha = Usuario.builder()
                .id(1L)
                .nome("Ana")
                .email("ana@clareza.dev")
                .senhaHash(HASH)
                .build();

        Usuario vinculado = comSenha.vincularGoogle("google-123");

        assertThat(vinculado.getGoogleId()).isEqualTo("google-123");
        assertThat(vinculado.getSenhaHash()).isEqualTo(HASH);
        assertThat(vinculado.getId()).isEqualTo(1L);
        assertThat(comSenha.vinculadoAoGoogle()).isFalse();
    }

    @Test
    @DisplayName("vincular o google sem identificador e recusado")
    void deveRecusarVinculo_quandoOIdentificadorDoGoogleEVazio() {
        Usuario usuario = Usuario.builder()
                .nome("Ana")
                .email("ana@clareza.dev")
                .senhaHash(HASH)
                .build();

        assertThatThrownBy(() -> usuario.vincularGoogle(" "))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}