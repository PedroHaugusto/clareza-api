package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeTransacao;
import com.clareza.application.port.out.CategoriaRepositoryPort;
import com.clareza.application.port.out.ContaRepositoryPort;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.model.Categoria;
import com.clareza.domain.model.Conta;
import com.clareza.domain.model.StatusTransacao;
import com.clareza.domain.model.TipoCategoria;
import com.clareza.domain.model.TipoConta;
import com.clareza.domain.model.TipoTransacao;
import com.clareza.domain.model.Transacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarTransacoesTest {

    private static final LocalDate DATA = LocalDate.of(2026, 9, 10);

    @Mock
    private TransacaoRepositoryPort transacaoRepository;

    @Mock
    private ContaRepositoryPort contaRepository;

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @InjectMocks
    private GerenciarTransacoes gerenciarTransacoes;

    @Test
    @DisplayName("transacao sem data de efetivacao nasce prevista")
    void deveCriarComoPrevista() {
        prepararContaECategoriaValidas();
        when(transacaoRepository.salvar(any(Transacao.class))).thenAnswer(c -> c.getArgument(0));

        gerenciarTransacoes.criar(comando().build());

        ArgumentCaptor<Transacao> capturada = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).salvar(capturada.capture());

        assertThat(capturada.getValue().getStatus()).isEqualTo(StatusTransacao.PREVISTA);
        assertThat(capturada.getValue().getDataEfetivacao()).isNull();
    }

    @Test
    @DisplayName("informar data de efetivacao ja lanca como confirmada")
    void deveCriarComoConfirmada_quandoInformaDataDeEfetivacao() {
        prepararContaECategoriaValidas();
        when(transacaoRepository.salvar(any(Transacao.class))).thenAnswer(c -> c.getArgument(0));

        gerenciarTransacoes.criar(comando().dataEfetivacao(DATA).build());

        ArgumentCaptor<Transacao> capturada = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).salvar(capturada.capture());

        assertThat(capturada.getValue().getStatus()).isEqualTo(StatusTransacao.CONFIRMADA);
    }

    @Test
    @DisplayName("conta de outro usuario e tratada como inexistente")
    void deveRecusarContaDeOutroUsuario() {
        Conta deOutro = Conta.builder()
                .id(10L).usuarioId(2L).nome("Nubank").tipo(TipoConta.CARTAO_CREDITO).build();
        when(contaRepository.buscarPorId(10L)).thenReturn(Optional.of(deOutro));

        assertThatThrownBy(() -> gerenciarTransacoes.criar(comando().build()))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Conta");

        verify(transacaoRepository, never()).salvar(any(Transacao.class));
    }

    @Test
    @DisplayName("categoria de outro usuario e tratada como inexistente")
    void deveRecusarCategoriaDeOutroUsuario() {
        when(contaRepository.buscarPorId(10L)).thenReturn(Optional.of(contaDoUsuario()));
        Categoria deOutro = Categoria.builder()
                .id(20L).usuarioId(2L).nome("Pets").tipo(TipoCategoria.DESPESA).corHex("#AD1457").build();
        when(categoriaRepository.buscarPorId(20L)).thenReturn(Optional.of(deOutro));

        assertThatThrownBy(() -> gerenciarTransacoes.criar(comando().build()))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Categoria");
    }

    @Test
    @DisplayName("categoria padrao do sistema pode ser usada por qualquer usuario")
    void deveAceitarCategoriaPadraoDoSistema() {
        when(contaRepository.buscarPorId(10L)).thenReturn(Optional.of(contaDoUsuario()));
        Categoria global = Categoria.builder()
                .id(20L).nome("Moradia").tipo(TipoCategoria.DESPESA).corHex("#6D4C41").build();
        when(categoriaRepository.buscarPorId(20L)).thenReturn(Optional.of(global));
        when(transacaoRepository.salvar(any(Transacao.class))).thenAnswer(c -> c.getArgument(0));

        gerenciarTransacoes.criar(comando().build());

        verify(transacaoRepository).salvar(any(Transacao.class));
    }

    @Test
    @DisplayName("editar preserva o vinculo de parcelamento, que o corpo da requisicao nao carrega")
    void devePreservarOsDadosDeParcelamentoAoEditar() {
        UUID grupo = UUID.randomUUID();
        Transacao existente = Transacao.builder()
                .id(7L).usuarioId(1L).contaId(10L).categoriaId(20L)
                .descricao("Geladeira 1/3").valor(new BigDecimal("400.00"))
                .tipo(TipoTransacao.DESPESA).dataPrevista(DATA)
                .grupoParcelamentoId(grupo).numeroParcela(1).totalParcelas(3)
                .build();
        when(transacaoRepository.buscarPorId(7L)).thenReturn(Optional.of(existente));
        prepararContaECategoriaValidas();
        when(transacaoRepository.salvar(any(Transacao.class))).thenAnswer(c -> c.getArgument(0));

        gerenciarTransacoes.editar(7L, comando().descricao("Geladeira nova").build());

        ArgumentCaptor<Transacao> capturada = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).salvar(capturada.capture());

        assertThat(capturada.getValue().getId()).isEqualTo(7L);
        assertThat(capturada.getValue().getDescricao()).isEqualTo("Geladeira nova");
        assertThat(capturada.getValue().getGrupoParcelamentoId()).isEqualTo(grupo);
        assertThat(capturada.getValue().getNumeroParcela()).isEqualTo(1);
        assertThat(capturada.getValue().getTotalParcelas()).isEqualTo(3);
    }

    @Test
    @DisplayName("transacao de outro usuario nao pode ser editada nem excluida")
    void deveIsolarTransacaoDeOutroUsuario() {
        Transacao deOutro = Transacao.builder()
                .id(7L).usuarioId(2L).contaId(10L).categoriaId(20L)
                .descricao("Aluguel").valor(new BigDecimal("100.00"))
                .tipo(TipoTransacao.DESPESA).dataPrevista(DATA).build();
        when(transacaoRepository.buscarPorId(7L)).thenReturn(Optional.of(deOutro));

        assertThatThrownBy(() -> gerenciarTransacoes.excluir(7L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(transacaoRepository, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("transacao propria e excluida")
    void deveExcluirTransacaoPropria() {
        Transacao propria = Transacao.builder()
                .id(7L).usuarioId(1L).contaId(10L).categoriaId(20L)
                .descricao("Aluguel").valor(new BigDecimal("100.00"))
                .tipo(TipoTransacao.DESPESA).dataPrevista(DATA).build();
        when(transacaoRepository.buscarPorId(7L)).thenReturn(Optional.of(propria));

        gerenciarTransacoes.excluir(7L, 1L);

        verify(transacaoRepository).excluir(7L);
    }

    private void prepararContaECategoriaValidas() {
        when(contaRepository.buscarPorId(10L)).thenReturn(Optional.of(contaDoUsuario()));
        when(categoriaRepository.buscarPorId(20L)).thenReturn(Optional.of(categoriaDoUsuario()));
    }

    private Conta contaDoUsuario() {
        return Conta.builder()
                .id(10L).usuarioId(1L).nome("Conta principal").tipo(TipoConta.CONTA_CORRENTE).build();
    }

    private Categoria categoriaDoUsuario() {
        return Categoria.builder()
                .id(20L).usuarioId(1L).nome("Pets").tipo(TipoCategoria.DESPESA).corHex("#AD1457").build();
    }

    private ComandoDeTransacao.ComandoDeTransacaoBuilder comando() {
        return ComandoDeTransacao.builder()
                .usuarioId(1L)
                .contaId(10L)
                .categoriaId(20L)
                .descricao("Conta de luz")
                .valor(new BigDecimal("150.00"))
                .tipo(TipoTransacao.DESPESA)
                .dataPrevista(DATA);
    }
}
