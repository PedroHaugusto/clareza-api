package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransacaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 8, 15);

    @Test
    @DisplayName("prevista com data no passado e lida como atrasada, sem depender de job")
    void deveDerivarAtrasada_quandoADataPrevistaJaPassou() {
        Transacao vencida = prevista(HOJE.minusDays(1));

        assertThat(vencida.getStatus()).isEqualTo(StatusTransacao.PREVISTA);
        assertThat(vencida.statusEm(HOJE)).isEqualTo(StatusTransacao.ATRASADA);
    }

    @Test
    @DisplayName("prevista para hoje ainda nao esta atrasada")
    void naoDeveAtrasar_quandoVenceHoje() {
        assertThat(prevista(HOJE).statusEm(HOJE)).isEqualTo(StatusTransacao.PREVISTA);
    }

    @Test
    @DisplayName("prevista para o futuro segue prevista")
    void naoDeveAtrasar_quandoVenceNoFuturo() {
        assertThat(prevista(HOJE.plusDays(10)).statusEm(HOJE)).isEqualTo(StatusTransacao.PREVISTA);
    }

    @Test
    @DisplayName("confirmada no passado nao vira atrasada")
    void naoDeveAtrasar_quandoJaFoiConfirmada() {
        Transacao confirmada = base()
                .dataPrevista(HOJE.minusDays(30))
                .dataEfetivacao(HOJE.minusDays(28))
                .status(StatusTransacao.CONFIRMADA)
                .build();

        assertThat(confirmada.statusEm(HOJE)).isEqualTo(StatusTransacao.CONFIRMADA);
        assertThat(confirmada.estaConfirmada()).isTrue();
    }

    @Test
    @DisplayName("atrasada nunca pode ser gravada, so derivada")
    void deveRecusarStatusAtrasado() {
        assertThatThrownBy(() -> base().dataPrevista(HOJE).status(StatusTransacao.ATRASADA).build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("derivado da data");
    }

    @Test
    @DisplayName("valor precisa ser positivo, porque o sinal vem do tipo")
    void deveRecusarValorZeroOuNegativo() {
        assertThatThrownBy(() -> base().dataPrevista(HOJE).valor(BigDecimal.ZERO).build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("positivo");

        assertThatThrownBy(() -> base().dataPrevista(HOJE).valor(new BigDecimal("-10.00")).build())
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("confirmada sem data de efetivacao e incoerente")
    void deveRecusarConfirmadaSemDataDeEfetivacao() {
        assertThatThrownBy(() -> base()
                .dataPrevista(HOJE).status(StatusTransacao.CONFIRMADA).build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("data de efetivacao");
    }

    @Test
    @DisplayName("prevista com data de efetivacao tambem e incoerente")
    void deveRecusarPrevistaComDataDeEfetivacao() {
        assertThatThrownBy(() -> base()
                .dataPrevista(HOJE)
                .dataEfetivacao(HOJE)
                .status(StatusTransacao.PREVISTA)
                .build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Somente transacao confirmada");
    }

    @Test
    @DisplayName("campos obrigatorios sao exigidos no proprio construtor")
    void deveExigirOsCamposObrigatorios() {
        assertThatThrownBy(() -> base().dataPrevista(HOJE).descricao("  ").build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("descricao");

        assertThatThrownBy(() -> base().dataPrevista(null).build())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("data prevista");
    }

    @Test
    @DisplayName("transacao sem grupo de parcelamento nao e parcelada")
    void deveIdentificarTransacaoAvulsa() {
        assertThat(prevista(HOJE).ehParcelada()).isFalse();
    }

    @Test
    @DisplayName("transacao pertence apenas ao proprio dono")
    void devePertencerApenasAoDono() {
        assertThat(prevista(HOJE).pertenceA(1L)).isTrue();
        assertThat(prevista(HOJE).pertenceA(2L)).isFalse();
    }

    private Transacao prevista(LocalDate dataPrevista) {
        return base().dataPrevista(dataPrevista).build();
    }

    private Transacao.TransacaoBuilder base() {
        return Transacao.builder()
                .usuarioId(1L)
                .contaId(10L)
                .categoriaId(20L)
                .descricao("Conta de luz")
                .valor(new BigDecimal("150.00"))
                .tipo(TipoTransacao.DESPESA);
    }
}
