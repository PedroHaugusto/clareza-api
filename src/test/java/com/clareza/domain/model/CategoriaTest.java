package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoriaTest {

    @Test
    @DisplayName("categoria sem dono e padrao do sistema")
    void deveIdentificarCategoriaPadraoDoSistema() {
        Categoria padrao = Categoria.builder()
                .nome("Salario").tipo(TipoCategoria.RECEITA).corHex("#2E7D32").build();

        assertThat(padrao.ehPadraoDoSistema()).isTrue();
        assertThat(padrao.pertenceA(1L)).isFalse();
    }

    @Test
    @DisplayName("categoria de um usuario nao pertence a outro")
    void naoDevePertencerAOutroUsuario() {
        Categoria doUsuario = Categoria.builder()
                .usuarioId(1L).nome("Pets").tipo(TipoCategoria.DESPESA).corHex("#AD1457").build();

        assertThat(doUsuario.pertenceA(1L)).isTrue();
        assertThat(doUsuario.pertenceA(2L)).isFalse();
        assertThat(doUsuario.ehPadraoDoSistema()).isFalse();
    }

    @Test
    @DisplayName("cor e guardada em maiusculas para nao duplicar #abc123 e #ABC123")
    void deveNormalizarACorParaMaiusculas() {
        Categoria categoria = Categoria.builder()
                .usuarioId(1L).nome("Pets").tipo(TipoCategoria.DESPESA).corHex("#ad1457").build();

        assertThat(categoria.getCorHex()).isEqualTo("#AD1457");
    }

    @Test
    @DisplayName("cor fora do formato #RRGGBB e recusada")
    void deveRecusarCorInvalida() {
        assertThatThrownBy(() -> Categoria.builder()
                .usuarioId(1L).nome("Pets").tipo(TipoCategoria.DESPESA).corHex("vermelho").build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("#RRGGBB");

        assertThatThrownBy(() -> Categoria.builder()
                .usuarioId(1L).nome("Pets").tipo(TipoCategoria.DESPESA).corHex("#AD145").build())
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nome e tipo sao obrigatorios")
    void deveRecusarNomeOuTipoAusente() {
        assertThatThrownBy(() -> Categoria.builder()
                .usuarioId(1L).nome("  ").tipo(TipoCategoria.DESPESA).corHex("#AD1457").build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("nome");

        assertThatThrownBy(() -> Categoria.builder()
                .usuarioId(1L).nome("Pets").corHex("#AD1457").build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("tipo");
    }

    @Test
    @DisplayName("nome perde os espacos das pontas")
    void deveRemoverEspacosDoNome() {
        Categoria categoria = Categoria.builder()
                .usuarioId(1L).nome("  Pets  ").tipo(TipoCategoria.DESPESA).corHex("#AD1457").build();

        assertThat(categoria.getNome()).isEqualTo("Pets");
    }
}
