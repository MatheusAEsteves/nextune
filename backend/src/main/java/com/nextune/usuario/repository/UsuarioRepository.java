package com.nextune.usuario.repository;

import com.nextune.usuario.entity.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioModel, UUID> {
    Optional<UsuarioModel> findByEmail(String email);
    boolean existsByEmail(String email);
}
