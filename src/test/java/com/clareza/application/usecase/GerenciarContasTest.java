package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeCriacaoDeConta;
import com.clareza.application.port.out.ContaRepositoryPort;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.exception.RegraDeNegocioException;
import com.clareza.domain.model.Conta;
import com.clareza.domain.model.TipoConta;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarContasTest {

    @Mock
    private ContaRepositoryPort contaRepository;

    @Mock
    private TransacaoRepositoryPort transacaoRepository;

    @InjectMocks
    private GerenciarContas gerenciarContas;

    @Test
    @DisplayName("conta criada nasce amarrada ao usuario autenticado")
    void deveCriarContaDoUsuario() {
        when(contaRepository.existeComNomeDoUsuario("Nubank", 1L)).thenReturn(false);
        when(contaRepository.salvar(any(Conta.class))).thenAnswer(c -> c.getArgument(0));

        gerenciarContas.criar(new ComandoDeCriacaoDeConta(1L, "Nubank", TipoConta.CARTAO_CREDITO));

        ArgumentCaptor<Conta> capturada = ArgumentCaptor.forClass(Conta.class);
        verify(contaRepository).salvar(capturada.capture());

        assertThat(capturada.getValue().getUsuarioId()).isEqualTo(1L);
        assertThat(capturada.getValue().ehCartaoDeCredito()).isTrue();
    }

    @Test
    @DisplayName("nome repetido do mesmo usuario e recusado antes de gravar")
    void deveRecusarNomeDuplicado() {
        when(contaRepository.existeComNomeDoUsuario("Conta principal", 1L)).thenReturn(true);

        assertThatThrownBy(() -> gerenciarContas.criar(
                new ComandoDeCriacaoDeConta(1L, "Conta principal", TipoConta.CONTA_CORRENTE)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Ja existe uma conta com este nome");

        verify(contaRepository, never()).salvar(any(Conta.class));
    }

    @Test
    @DisplayName("conta de outro usuario responde 404, sem revelar que existe")
    void deveTratarContaDeOutroUsuarioComoInexistente() {
        Conta deOutro = Conta.builder()
                .id(9L).usuarioId(2L).nome("Nubank").tipo(TipoConta.CARTAO_CREDITO).build();
        when(contaRepository.buscarPorId(9L)).thenReturn(Optional.of(deOutro));

        assertThatThrownBy(() -> gerenciarContas.excluir(9L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(contaRepository, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("conta inexistente responde 404")
    void deveRecusarExclusaoDeContaInexistente() {
        when(contaRepository.buscarPorId(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gerenciarContas.excluir(404L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("conta propria e sem lancamentos e excluida")
    void deveExcluirContaDoProprioUsuario() {
        Conta propria = Conta.builder()
                .id(9L).usuarioId(1L).nome("Nubank").tipo(TipoConta.CARTAO_CREDITO).build();
        when(contaRepository.buscarPorId(9L)).thenReturn(Optional.of(propria));
        when(transacaoRepository.existeComConta(9L)).thenReturn(false);

        gerenciarContas.excluir(9L, 1L);

        verify(contaRepository).excluir(9L);
    }

    @Test
    @DisplayName("conta com lancamentos responde 422 em vez de estourar a chave estrangeira")
    void deveRecusarExclusaoDeContaEmUso() {
        Conta propria = Conta.builder()
                .id(9L).usuarioId(1L).nome("Nubank").tipo(TipoConta.CARTAO_CREDITO).build();
        when(contaRepository.buscarPorId(9L)).thenReturn(Optional.of(propria));
        when(transacaoRepository.existeComConta(9L)).thenReturn(true);

        assertThatThrownBy(() -> gerenciarContas.excluir(9L, 1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("tem lancamentos");

        verify(contaRepository, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("listagem sempre passa pelo filtro de usuario")
    void deveListarSempreFiltrandoPorUsuario() {
        when(contaRepository.listarDoUsuario(1L)).thenReturn(java.util.Collections.emptyList());

        gerenciarContas.listar(1L);

        verify(contaRepository).listarDoUsuario(1L);
    }
}
