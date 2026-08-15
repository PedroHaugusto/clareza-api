package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeCriacaoDeCategoria;
import com.clareza.application.port.out.CategoriaRepositoryPort;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.exception.RegraDeNegocioException;
import com.clareza.domain.model.Categoria;
import com.clareza.domain.model.TipoCategoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarCategoriasTest {

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @Mock
    private TransacaoRepositoryPort transacaoRepository;

    @InjectMocks
    private GerenciarCategorias gerenciarCategorias;

    @Test
    @DisplayName("categoria criada nasce amarrada ao usuario autenticado")
    void deveCriarCategoriaDoUsuario() {
        when(categoriaRepository.existeComNomeVisivelPara("Pets", 1L)).thenReturn(false);
        when(categoriaRepository.salvar(any(Categoria.class))).thenAnswer(c -> c.getArgument(0));

        gerenciarCategorias.criar(new ComandoDeCriacaoDeCategoria(
                1L, "Pets", TipoCategoria.DESPESA, "#AD1457"));

        ArgumentCaptor<Categoria> capturada = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).salvar(capturada.capture());

        assertThat(capturada.getValue().getUsuarioId()).isEqualTo(1L);
        assertThat(capturada.getValue().ehPadraoDoSistema()).isFalse();
    }

    @Test
    @DisplayName("nome repetido e recusado antes de gravar")
    void deveRecusarNomeDuplicado() {
        when(categoriaRepository.existeComNomeVisivelPara("Moradia", 1L)).thenReturn(true);

        assertThatThrownBy(() -> gerenciarCategorias.criar(new ComandoDeCriacaoDeCategoria(
                1L, "Moradia", TipoCategoria.DESPESA, "#6D4C41")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Ja existe uma categoria com este nome");

        verify(categoriaRepository, never()).salvar(any(Categoria.class));
    }

    @Test
    @DisplayName("categoria padrao do sistema nao pode ser excluida")
    void deveRecusarExclusaoDeCategoriaPadrao() {
        Categoria padrao = Categoria.builder()
                .id(5L).nome("Moradia").tipo(TipoCategoria.DESPESA).corHex("#6D4C41").build();
        when(categoriaRepository.buscarPorId(5L)).thenReturn(Optional.of(padrao));

        assertThatThrownBy(() -> gerenciarCategorias.excluir(5L, 1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("padrao do sistema");

        verify(categoriaRepository, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("categoria de outro usuario responde 404, sem revelar que existe")
    void deveTratarCategoriaDeOutroUsuarioComoInexistente() {
        Categoria deOutro = Categoria.builder()
                .id(9L).usuarioId(2L).nome("Pets").tipo(TipoCategoria.DESPESA).corHex("#AD1457").build();
        when(categoriaRepository.buscarPorId(9L)).thenReturn(Optional.of(deOutro));

        assertThatThrownBy(() -> gerenciarCategorias.excluir(9L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(categoriaRepository, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("categoria inexistente responde 404")
    void deveRecusarExclusaoDeCategoriaInexistente() {
        when(categoriaRepository.buscarPorId(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gerenciarCategorias.excluir(404L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("categoria propria e sem lancamentos e excluida")
    void deveExcluirCategoriaDoProprioUsuario() {
        Categoria propria = Categoria.builder()
                .id(9L).usuarioId(1L).nome("Pets").tipo(TipoCategoria.DESPESA).corHex("#AD1457").build();
        when(categoriaRepository.buscarPorId(9L)).thenReturn(Optional.of(propria));
        when(transacaoRepository.existeComCategoria(9L)).thenReturn(false);

        gerenciarCategorias.excluir(9L, 1L);

        verify(categoriaRepository).excluir(9L);
    }

    @Test
    @DisplayName("categoria com lancamentos responde 422 em vez de estourar a chave estrangeira")
    void deveRecusarExclusaoDeCategoriaEmUso() {
        Categoria propria = Categoria.builder()
                .id(9L).usuarioId(1L).nome("Pets").tipo(TipoCategoria.DESPESA).corHex("#AD1457").build();
        when(categoriaRepository.buscarPorId(9L)).thenReturn(Optional.of(propria));
        when(transacaoRepository.existeComCategoria(9L)).thenReturn(true);

        assertThatThrownBy(() -> gerenciarCategorias.excluir(9L, 1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("tem lancamentos");

        verify(categoriaRepository, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("listagem sempre passa pelo filtro de usuario")
    void deveListarSempreFiltrandoPorUsuario() {
        when(categoriaRepository.listarVisiveisPara(1L)).thenReturn(java.util.Collections.emptyList());

        gerenciarCategorias.listar(1L);

        verify(categoriaRepository).listarVisiveisPara(1L);
        verify(categoriaRepository, never()).existeComNomeVisivelPara(anyString(), anyLong());
    }
}
