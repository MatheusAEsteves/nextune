package com.nextune.comentario.repository;

import com.nextune.comentario.entity.ComentarioModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComentarioRepository extends JpaRepository<ComentarioModel, UUID> {

    @EntityGraph(attributePaths = "usuario")
    Page<ComentarioModel> findByPostIdOrderByDataComentarioDesc(UUID postId, Pageable pageable);
}
