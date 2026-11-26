package com.nextune.denuncia.repository;

import com.nextune.denuncia.entity.DenunciaModel;
import com.nextune.denuncia.entity.DenunciaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DenunciaRepository extends JpaRepository<DenunciaModel, UUID> {

    boolean existsByUsuarioIdAndPostIdAndStatus(UUID usuarioId, UUID postId, DenunciaStatus status);

    boolean existsByUsuarioIdAndArtistaIdAndStatus(UUID usuarioId, UUID artistaId, DenunciaStatus status);

    Page<DenunciaModel> findByStatus(DenunciaStatus status, Pageable pageable);
}
