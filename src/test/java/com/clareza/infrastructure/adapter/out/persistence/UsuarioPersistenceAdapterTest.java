package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.TesteDeIntegracao;
import com.clareza.application.port.out.UsuarioRepositoryPort;
import com.clareza.domain.model.Usuario;
import com.clareza.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class UsuarioPersistenceAdapterTest extends TesteDeIntegracao {

    private static final String HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Autowired
    private UsuarioRepositoryPort repositorio;

    @Autowired
    private UsuarioJpaRepository jpaRepository;

    @Test
    @DisplayName("usuario salvo recebe id e volta identico na busca")
    void deveSalvarERecuperarPorId() {
        Usuario salvo = repositorio.salvar(comSenha("ana@clareza.dev"));

        assertThat(salvo.getId()).isNotNull();

        Optional<Usuario> encontrado = repositorio.buscarPorId(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Ana");
        assertThat(encontrado.get().getEmail()).isEqualTo("ana@clareza.dev");
        assertThat(encontrado.get().getSenhaHash()).isEqualTo(HASH);
    }

    @Test
    @DisplayName("busca por email ignora maiusculas e espacos digitados no login")
    void deveEncontrarPorEmail_mesmoComMaiusculasEEspacos() {
        repositorio.salvar(comSenha("ana@clareza.dev"));

        assertThat(repositorio.buscarPorEmail("  ANA@Clareza.dev ")).isPresent();
    }

    @Test
    @DisplayName("busca por googleId encontra quem entrou pelo login social")
    void deveEncontrarPorGoogleId() {
        repositorio.salvar(Usuario.builder()
                .nome("Bruno")
                .email("bruno@clareza.dev")
                .googleId("google-987")
                .build());

        Optional<Usuario> encontrado = repositorio.buscarPorGoogleId("google-987");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().possuiSenha()).isFalse();
        assertThat(encontrado.get().getEmail()).isEqualTo("bruno@clareza.dev");
    }

    @Test
    @DisplayName("existePorEmail responde sem carregar o usuario inteiro")
    void deveInformarSeOEmailJaExiste() {
        repositorio.salvar(comSenha("ana@clareza.dev"));

        assertThat(repositorio.existePorEmail("ANA@clareza.dev")).isTrue();
        assertThat(repositorio.existePorEmail("outra@clareza.dev")).isFalse();
    }

    @Test
    @DisplayName("buscas por dado inexistente voltam vazias, sem lancar excecao")
    void deveRetornarVazio_quandoNaoExiste() {
        assertThat(repositorio.buscarPorEmail("ninguem@clareza.dev")).isEmpty();
        assertThat(repositorio.buscarPorGoogleId("google-inexistente")).isEmpty();
        assertThat(repositorio.buscarPorId(999_999L)).isEmpty();
    }

    @Test
    @DisplayName("o banco recusa dois usuarios com o mesmo email")
    void deveRecusarEmailDuplicado() {
        repositorio.salvar(comSenha("ana@clareza.dev"));
        jpaRepository.flush();

        assertThatThrownBy(() -> {
            repositorio.salvar(comSenha("ana@clareza.dev"));
            jpaRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Usuario comSenha(String email) {
        return Usuario.builder()
                .nome("Ana")
                .email(email)
                .senhaHash(HASH)
                .build();
    }
}