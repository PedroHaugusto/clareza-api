package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.application.port.out.UsuarioRepositoryPort;
import com.clareza.domain.model.Usuario;
import com.clareza.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.clareza.infrastructure.adapter.out.persistence.mapper.UsuarioPersistenceMapper;
import com.clareza.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repository;
    private final UsuarioPersistenceMapper mapper;

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity entidade = repository.save(mapper.paraEntidade(usuario));
        return mapper.paraDominio(entidade);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(normalizar(email)).map(mapper::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorGoogleId(String googleId) {
        return repository.findByGoogleId(googleId).map(mapper::paraDominio);
    }

    @Override
    public boolean existePorEmail(String email) {
        return repository.existsByEmail(normalizar(email));
    }

    private String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
