package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeLoginComGoogle;
import com.clareza.application.port.in.UsuarioAutenticado;
import com.clareza.application.port.out.GeradorDeTokenPort;
import com.clareza.application.port.out.UsuarioRepositoryPort;
import com.clareza.application.port.out.ValidadorDeTokenGooglePort;
import com.clareza.domain.exception.AutenticacaoGoogleException;
import com.clareza.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticarComGoogleTest {

    private static final Instant VENCIMENTO = Instant.parse("2026-08-15T18:00:00Z");
    private static final String GOOGLE_ID = "google-123";
    private static final String EMAIL = "ana@clareza.dev";

    @Mock
    private ValidadorDeTokenGooglePort validadorDeTokenGoogle;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private GeradorDeTokenPort geradorDeToken;

    @InjectMocks
    private AutenticarComGoogle autenticarComGoogle;

    @Test
    @DisplayName("quem ja entrou pelo google antes apenas recebe um token novo")
    void deveAutenticarUsuarioJaVinculado() {
        Usuario existente = Usuario.builder()
                .id(42L).nome("Ana").email(EMAIL).googleId(GOOGLE_ID).build();
        when(validadorDeTokenGoogle.validar("id-token")).thenReturn(contaGoogle());
        when(usuarioRepository.buscarPorGoogleId(GOOGLE_ID)).thenReturn(Optional.of(existente));
        when(geradorDeToken.gerarPara(existente))
                .thenReturn(new GeradorDeTokenPort.TokenGerado("jwt-gerado", VENCIMENTO));

        UsuarioAutenticado resultado =
                autenticarComGoogle.autenticar(new ComandoDeLoginComGoogle("id-token"));

        assertThat(resultado.getId()).isEqualTo(42L);
        assertThat(resultado.getToken()).isEqualTo("jwt-gerado");
        verify(usuarioRepository, never()).salvar(any(Usuario.class));
    }

    @Test
    @DisplayName("primeiro acesso pelo google cria a conta sem senha")
    void deveCriarConta_quandoOEmailAindaNaoExiste() {
        when(validadorDeTokenGoogle.validar("id-token")).thenReturn(contaGoogle());
        when(usuarioRepository.buscarPorGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.empty());
        when(usuarioRepository.salvar(any(Usuario.class)))
                .thenAnswer(chamada -> ((Usuario) chamada.getArgument(0)).toBuilder().id(7L).build());
        when(geradorDeToken.gerarPara(any(Usuario.class)))
                .thenReturn(new GeradorDeTokenPort.TokenGerado("jwt-gerado", VENCIMENTO));

        UsuarioAutenticado resultado =
                autenticarComGoogle.autenticar(new ComandoDeLoginComGoogle("id-token"));

        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).salvar(capturado.capture());

        assertThat(capturado.getValue().possuiSenha()).isFalse();
        assertThat(capturado.getValue().getGoogleId()).isEqualTo(GOOGLE_ID);
        assertThat(capturado.getValue().getNome()).isEqualTo("Ana Souza");
        assertThat(resultado.getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("e-mail ja cadastrado com senha ganha o vinculo do google, sem perder a senha")
    void deveVincularAContaExistente_preservandoASenha() {
        Usuario comSenha = Usuario.builder()
                .id(42L).nome("Ana").email(EMAIL).senhaHash("hash-bcrypt").build();
        when(validadorDeTokenGoogle.validar("id-token")).thenReturn(contaGoogle());
        when(usuarioRepository.buscarPorGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(comSenha));
        when(usuarioRepository.salvar(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(geradorDeToken.gerarPara(any(Usuario.class)))
                .thenReturn(new GeradorDeTokenPort.TokenGerado("jwt-gerado", VENCIMENTO));

        autenticarComGoogle.autenticar(new ComandoDeLoginComGoogle("id-token"));

        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).salvar(capturado.capture());

        assertThat(capturado.getValue().getId()).isEqualTo(42L);
        assertThat(capturado.getValue().getGoogleId()).isEqualTo(GOOGLE_ID);
        assertThat(capturado.getValue().getSenhaHash()).isEqualTo("hash-bcrypt");
    }

    @Test
    @DisplayName("token recusado pelo google nao cria nem altera conta alguma")
    void naoDeveTocarNoBanco_quandoOTokenEInvalido() {
        when(validadorDeTokenGoogle.validar("id-token-falso"))
                .thenThrow(new AutenticacaoGoogleException("Nao foi possivel validar o login com o Google"));

        assertThatThrownBy(() -> autenticarComGoogle.autenticar(
                new ComandoDeLoginComGoogle("id-token-falso")))
                .isInstanceOf(AutenticacaoGoogleException.class);

        verify(usuarioRepository, never()).salvar(any(Usuario.class));
        verify(usuarioRepository, never()).buscarPorGoogleId(anyString());
        verify(geradorDeToken, never()).gerarPara(any(Usuario.class));
    }

    private ValidadorDeTokenGooglePort.ContaGoogle contaGoogle() {
        return new ValidadorDeTokenGooglePort.ContaGoogle(GOOGLE_ID, EMAIL, "Ana Souza");
    }
}
