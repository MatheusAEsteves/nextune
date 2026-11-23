package com.nextune.artista.repository;

import com.nextune.artista.entity.ArtistaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArtistaRepository extends JpaRepository<ArtistaModel, UUID> {
    boolean existsByUsuarioId(UUID usuarioId);
    Optional<ArtistaModel> findByUsuarioId(UUID usuarioId);
}
