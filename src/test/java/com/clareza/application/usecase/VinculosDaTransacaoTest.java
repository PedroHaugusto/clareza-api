package com.clareza.application.usecase;

import com.clareza.application.port.out.CategoriaRepositoryPort;
import com.clareza.application.port.out.ContaRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.model.Categoria;
import com.clareza.domain.model.Conta;
import com.clareza.domain.model.TipoCategoria;
import com.clareza.domain.model.TipoConta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VinculosDaTransacaoTest {

    @Mock
    private ContaRepositoryPort contaRepository;

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @InjectMocks
    private VinculosDaTransacao vinculos;

    @Test
    @DisplayName("conta do proprio usuario passa")
    void deveAceitarContaPropria() {
        when(contaRepository.buscarPorId(10L)).thenReturn(Optional.of(Conta.builder()
                .id(10L).usuarioId(1L).nome("Conta principal").tipo(TipoConta.CONTA_CORRENTE).build()));

        assertThatCode(() -> vinculos.exigirContaDoUsuario(10L, 1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("conta de outro usuario e tratada como inexistente")
    void deveRecusarContaDeOutroUsuario() {
        when(contaRepository.buscarPorId(10L)).thenReturn(Optional.of(Conta.builder()
                .id(10L).usuarioId(2L).nome("Nubank").tipo(TipoConta.CARTAO_CREDITO).build()));

        assertThatThrownBy(() -> vinculos.exigirContaDoUsuario(10L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Conta");
    }

    @Test
    @DisplayName("conta inexistente responde 404")
    void deveRecusarContaInexistente() {
        when(contaRepository.buscarPorId(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vinculos.exigirContaDoUsuario(404L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("categoria padrao do sistema serve para qualquer usuario")
    void deveAceitarCategoriaPadraoDoSistema() {
        when(categoriaRepository.buscarPorId(20L)).thenReturn(Optional.of(Categoria.builder()
                .id(20L).nome("Moradia").tipo(TipoCategoria.DESPESA).corHex("#6D4C41").build()));

        assertThatCode(() -> vinculos.exigirCategoriaVisivel(20L, 1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("categoria de outro usuario e tratada como inexistente")
    void deveRecusarCategoriaDeOutroUsuario() {
        when(categoriaRepository.buscarPorId(20L)).thenReturn(Optional.of(Categoria.builder()
                .id(20L).usuarioId(2L).nome("Pets").tipo(TipoCategoria.DESPESA).corHex("#AD1457").build()));

        assertThatThrownBy(() -> vinculos.exigirCategoriaVisivel(20L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Categoria");
    }
}
