package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    Optional<UsuarioEntity> findByGoogleId(String googleId);

    boolean existsByEmail(String email);
}