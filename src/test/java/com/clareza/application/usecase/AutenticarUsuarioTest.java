package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeLogin;
import com.clareza.application.port.in.UsuarioAutenticado;
import com.clareza.application.port.out.CodificadorDeSenhaPort;
import com.clareza.application.port.out.GeradorDeTokenPort;
import com.clareza.application.port.out.UsuarioRepositoryPort;
import com.clareza.domain.exception.CredenciaisInvalidasException;
import com.clareza.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioTest {

    private static final Instant VENCIMENTO = Instant.parse("2026-08-15T18:00:00Z");

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private CodificadorDeSenhaPort codificadorDeSenha;

    @Mock
    private GeradorDeTokenPort geradorDeToken;

    @InjectMocks
    private AutenticarUsuario autenticarUsuario;

    @Test
    @DisplayName("senha correta devolve o token da aplicacao")
    void deveAutenticar_quandoASenhaConfere() {
        when(usuarioRepository.buscarPorEmail("ana@clareza.dev"))
                .thenReturn(Optional.of(usuarioComSenha()));
        when(codificadorDeSenha.confere("senha-secreta", "hash-bcrypt")).thenReturn(true);
        when(geradorDeToken.gerarPara(any(Usuario.class)))
                .thenReturn(new GeradorDeTokenPort.TokenGerado("jwt-gerado", VENCIMENTO));

        UsuarioAutenticado resultado =
                autenticarUsuario.autenticar(new ComandoDeLogin("ana@clareza.dev", "senha-secreta"));

        assertThat(resultado.getToken()).isEqualTo("jwt-gerado");
        assertThat(resultado.getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("e-mail inexistente responde a mesma mensagem da senha errada")
    void deveRecusar_quandoOEmailNaoExiste() {
        when(usuarioRepository.buscarPorEmail("ninguem@clareza.dev")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autenticarUsuario.autenticar(
                new ComandoDeLogin("ninguem@clareza.dev", "senha-secreta")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha invalidos");

        verify(geradorDeToken, never()).gerarPara(any(Usuario.class));
    }

    @Test
    @DisplayName("senha errada nao revela que o e-mail existe")
    void deveRecusar_quandoASenhaNaoConfere() {
        when(usuarioRepository.buscarPorEmail("ana@clareza.dev"))
                .thenReturn(Optional.of(usuarioComSenha()));
        when(codificadorDeSenha.confere("senha-errada", "hash-bcrypt")).thenReturn(false);

        assertThatThrownBy(() -> autenticarUsuario.autenticar(
                new ComandoDeLogin("ana@clareza.dev", "senha-errada")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha invalidos");
    }

    @Test
    @DisplayName("conta so do google nao entra por senha, e a recusa e indistinguivel das demais")
    void deveRecusar_quandoAContaSoTemGoogle() {
        Usuario apenasGoogle = Usuario.builder()
                .id(7L)
                .nome("Bruno")
                .email("bruno@clareza.dev")
                .googleId("google-987")
                .build();
        when(usuarioRepository.buscarPorEmail("bruno@clareza.dev"))
                .thenReturn(Optional.of(apenasGoogle));

        assertThatThrownBy(() -> autenticarUsuario.autenticar(
                new ComandoDeLogin("bruno@clareza.dev", "qualquer-senha")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha invalidos");

        verify(codificadorDeSenha, never()).confere(any(), any());
    }

    private Usuario usuarioComSenha() {
        return Usuario.builder()
                .id(42L)
                .nome("Ana")
                .email("ana@clareza.dev")
                .senhaHash("hash-bcrypt")
                .build();
    }
}
