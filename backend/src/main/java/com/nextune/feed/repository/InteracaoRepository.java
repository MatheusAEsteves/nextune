package com.nextune.feed.repository;

import com.nextune.feed.entity.InteracaoId;
import com.nextune.feed.entity.InteracaoModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InteracaoRepository extends JpaRepository<InteracaoModel, InteracaoId> {
    // A chave é @EmbeddedId: o caminho da propriedade é id.usuarioId => "IdUsuarioId"
    Page<InteracaoModel> findByIdUsuarioIdAndCurtidoTrueOrderByDataAtualizacaoDesc(UUID usuarioId, Pageable pageable);
    Page<InteracaoModel> findByIdUsuarioIdAndSalvoTrueOrderByDataAtualizacaoDesc(UUID usuarioId, Pageable pageable);
    long countByIdPostIdAndCurtidoTrue(UUID postId);
}
