package com.nextune.seguidor.repository;

import com.nextune.seguidor.entity.SeguidorModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SeguidorRepository extends JpaRepository<SeguidorModel, UUID> {

    boolean existsByUsuarioIdAndArtistaId(UUID usuarioId, UUID artistaId);

    long countByArtistaId(UUID artistaId);

    long deleteByUsuarioIdAndArtistaId(UUID usuarioId, UUID artistaId);

    @EntityGraph(attributePaths = "artista")
    Page<SeguidorModel> findByUsuarioIdOrderByDataSeguimentoDesc(UUID usuarioId, Pageable pageable);
}
