package com.nextune.artista;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArtistaRepository extends JpaRepository<ArtistaModel, UUID> {
    boolean existsByUsuarioID(UUID usuarioId);
    Optional<ArtistaModel> findByUsuarioId(UUID usuarioId);
}
