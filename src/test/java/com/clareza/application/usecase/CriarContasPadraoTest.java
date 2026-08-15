package com.clareza.application.usecase;

import com.clareza.application.port.out.ContaRepositoryPort;
import com.clareza.domain.model.Conta;
import com.clareza.domain.model.TipoConta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CriarContasPadraoTest {

    @Mock
    private ContaRepositoryPort contaRepository;

    @InjectMocks
    private CriarContasPadrao criarContasPadrao;

    @Test
    @DisplayName("semeia uma conta corrente e um cartao, ambos do usuario informado")
    void deveCriarAsDuasContasPadrao() {
        criarContasPadrao.criarPara(42L);

        ArgumentCaptor<Conta> capturadas = ArgumentCaptor.forClass(Conta.class);
        verify(contaRepository, times(2)).salvar(capturadas.capture());

        List<Conta> contas = capturadas.getAllValues();
        assertThat(contas).extracting(Conta::getUsuarioId).containsOnly(42L);
        assertThat(contas).extracting(Conta::getTipo)
                .containsExactly(TipoConta.CONTA_CORRENTE, TipoConta.CARTAO_CREDITO);
        assertThat(contas).extracting(Conta::getNome)
                .containsExactly("Conta principal", "Cartão principal");
    }
}
