package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContaTest {

    @Test
    @DisplayName("cartao de credito e reconhecido pelo tipo, sem entidade separada")
    void deveReconhecerCartaoDeCredito() {
        Conta cartao = Conta.builder()
                .usuarioId(1L).nome("Cartão principal").tipo(TipoConta.CARTAO_CREDITO).build();
        Conta corrente = Conta.builder()
                .usuarioId(1L).nome("Conta principal").tipo(TipoConta.CONTA_CORRENTE).build();

        assertThat(cartao.ehCartaoDeCredito()).isTrue();
        assertThat(corrente.ehCartaoDeCredito()).isFalse();
    }

    @Test
    @DisplayName("conta sempre tem dono, e nao pertence a mais ninguem")
    void devePertencerApenasAoProprioDono() {
        Conta conta = Conta.builder()
                .usuarioId(1L).nome("Carteira").tipo(TipoConta.CARTEIRA).build();

        assertThat(conta.pertenceA(1L)).isTrue();
        assertThat(conta.pertenceA(2L)).isFalse();
    }

    @Test
    @DisplayName("conta sem dono e recusada, diferente da categoria que aceita nulo")
    void deveRecusarContaSemUsuario() {
        assertThatThrownBy(() -> Conta.builder()
                .nome("Conta principal").tipo(TipoConta.CONTA_CORRENTE).build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("pertencer a um usuario");
    }

    @Test
    @DisplayName("nome e tipo sao obrigatorios")
    void deveRecusarNomeOuTipoAusente() {
        assertThatThrownBy(() -> Conta.builder()
                .usuarioId(1L).nome(" ").tipo(TipoConta.CARTEIRA).build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("nome");

        assertThatThrownBy(() -> Conta.builder()
                .usuarioId(1L).nome("Carteira").build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("tipo");
    }
}
