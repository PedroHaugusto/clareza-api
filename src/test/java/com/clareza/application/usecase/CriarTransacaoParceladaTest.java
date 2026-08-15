package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeParcelamento;
import com.clareza.application.port.out.TransacaoRepositoryPort;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarTransacaoParceladaTest {

    private static final LocalDate PRIMEIRA = LocalDate.of(2026, 8, 31);

    @Mock
    private TransacaoRepositoryPort transacaoRepository;

    @Mock
    private VinculosDaTransacao vinculos;

    @InjectMocks
    private CriarTransacaoParcelada criarTransacaoParcelada;

    @Test
    @DisplayName("gera uma transacao por parcela, numeradas e no mesmo grupo")
    void deveGerarUmaTransacaoPorParcela() {
        when(transacaoRepository.salvarTodas(anyList())).thenAnswer(c -> c.getArgument(0));

        criarTransacaoParcelada.parcelar(comando(new BigDecimal("1200.00"), 3));

        List<Transacao> parcelas = capturar();

        assertThat(parcelas).hasSize(3);
        assertThat(parcelas).extracting(Transacao::getNumeroParcela).containsExactly(1, 2, 3);
        assertThat(parcelas).extracting(Transacao::getTotalParcelas).containsOnly(3);
        assertThat(parcelas).extracting(Transacao::getValor)
                .containsExactly(new BigDecimal("400.00"), new BigDecimal("400.00"),
                        new BigDecimal("400.00"));

        List<UUID> grupos = parcelas.stream()
                .map(Transacao::getGrupoParcelamentoId)
                .distinct()
                .collect(Collectors.toList());
        assertThat(grupos).hasSize(1);
        assertThat(grupos.get(0)).isNotNull();
        assertThat(parcelas).allMatch(Transacao::ehParcelada);
    }

    @Test
    @DisplayName("cada parcela cai um mes depois da anterior")
    void deveAvancarUmMesPorParcela() {
        when(transacaoRepository.salvarTodas(anyList())).thenAnswer(c -> c.getArgument(0));

        criarTransacaoParcelada.parcelar(comando(new BigDecimal("300.00"), 3));

        assertThat(capturar()).extracting(Transacao::getDataPrevista)
                .containsExactly(
                        LocalDate.of(2026, 8, 31),
                        LocalDate.of(2026, 9, 30),
                        LocalDate.of(2026, 10, 31));
    }

    @Test
    @DisplayName("todas as parcelas nascem previstas")
    void deveGerarParcelasPrevistas() {
        when(transacaoRepository.salvarTodas(anyList())).thenAnswer(c -> c.getArgument(0));

        criarTransacaoParcelada.parcelar(comando(new BigDecimal("300.00"), 3));

        assertThat(capturar()).allMatch(parcela -> !parcela.estaConfirmada());
        assertThat(capturar()).extracting(Transacao::getDataEfetivacao).containsOnlyNulls();
    }

    @Test
    @DisplayName("conta e categoria sao validadas antes de gerar qualquer parcela")
    void deveValidarOsVinculos() {
        when(transacaoRepository.salvarTodas(anyList())).thenAnswer(c -> c.getArgument(0));

        criarTransacaoParcelada.parcelar(comando(new BigDecimal("300.00"), 3));

        verify(vinculos).exigirContaDoUsuario(10L, 1L);
        verify(vinculos).exigirCategoriaVisivel(20L, 1L);
    }

    private List<Transacao> capturar() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Transacao>> capturadas = ArgumentCaptor.forClass(List.class);
        verify(transacaoRepository).salvarTodas(capturadas.capture());
        return capturadas.getValue();
    }

    private ComandoDeParcelamento comando(BigDecimal valorTotal, int totalParcelas) {
        return ComandoDeParcelamento.builder()
                .usuarioId(1L)
                .contaId(10L)
                .categoriaId(20L)
                .descricao("Geladeira")
                .valorTotal(valorTotal)
                .tipo(TipoTransacao.DESPESA)
                .dataDaPrimeiraParcela(PRIMEIRA)
                .totalParcelas(totalParcelas)
                .build();
    }
}
