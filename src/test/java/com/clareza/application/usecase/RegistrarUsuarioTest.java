package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeRegistro;
import com.clareza.application.port.in.UsuarioAutenticado;
import com.clareza.application.port.out.CodificadorDeSenhaPort;
import com.clareza.application.port.out.GeradorDeTokenPort;
import com.clareza.application.port.out.UsuarioRepositoryPort;
import com.clareza.domain.exception.RegraDeNegocioException;
import com.clareza.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioTest {

    private static final Instant VENCIMENTO = Instant.parse("2026-08-15T18:00:00Z");

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private CodificadorDeSenhaPort codificadorDeSenha;

    @Mock
    private GeradorDeTokenPort geradorDeToken;

    @InjectMocks
    private RegistrarUsuario registrarUsuario;

    @Test
    @DisplayName("registro guarda a senha em hash e devolve o token ja emitido")
    void deveRegistrarEDevolverToken() {
        when(usuarioRepository.existePorEmail("ana@clareza.dev")).thenReturn(false);
        when(codificadorDeSenha.codificar("senha-secreta")).thenReturn("hash-bcrypt");
        when(usuarioRepository.salvar(any(Usuario.class))).thenAnswer(chamada -> {
            Usuario recebido = chamada.getArgument(0);
            return recebido.toBuilder().id(42L).build();
        });
        when(geradorDeToken.gerarPara(any(Usuario.class)))
                .thenReturn(new GeradorDeTokenPort.TokenGerado("jwt-gerado", VENCIMENTO));

        UsuarioAutenticado resultado = registrarUsuario.registrar(
                new ComandoDeRegistro("Ana", "ana@clareza.dev", "senha-secreta"));

        assertThat(resultado.getId()).isEqualTo(42L);
        assertThat(resultado.getEmail()).isEqualTo("ana@clareza.dev");
        assertThat(resultado.getToken()).isEqualTo("jwt-gerado");
        assertThat(resultado.getExpiraEm()).isEqualTo(VENCIMENTO);
    }

    @Test
    @DisplayName("a senha em texto puro nunca chega ao repositorio")
    void naoDevePersistirASenhaEmTextoPuro() {
        when(usuarioRepository.existePorEmail("ana@clareza.dev")).thenReturn(false);
        when(codificadorDeSenha.codificar("senha-secreta")).thenReturn("hash-bcrypt");
        when(usuarioRepository.salvar(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(geradorDeToken.gerarPara(any(Usuario.class)))
                .thenReturn(new GeradorDeTokenPort.TokenGerado("jwt-gerado", VENCIMENTO));

        registrarUsuario.registrar(new ComandoDeRegistro("Ana", "ana@clareza.dev", "senha-secreta"));

        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).salvar(capturado.capture());

        assertThat(capturado.getValue().getSenhaHash()).isEqualTo("hash-bcrypt");
        assertThat(capturado.getValue().getSenhaHash()).isNotEqualTo("senha-secreta");
    }

    @Test
    @DisplayName("e-mail ja cadastrado impede o registro antes de tocar o banco")
    void deveRecusarEmailJaCadastrado() {
        when(usuarioRepository.existePorEmail("ana@clareza.dev")).thenReturn(true);

        assertThatThrownBy(() -> registrarUsuario.registrar(
                new ComandoDeRegistro("Ana", "ana@clareza.dev", "senha-secreta")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Ja existe uma conta com este e-mail");

        verify(usuarioRepository, never()).salvar(any(Usuario.class));
        verify(geradorDeToken, never()).gerarPara(any(Usuario.class));
    }
}
