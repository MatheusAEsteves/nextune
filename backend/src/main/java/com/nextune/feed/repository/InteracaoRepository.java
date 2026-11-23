package com.nextune.feed.repository;

import com.nextune.feed.entity.InteracaoId;
import com.nextune.feed.entity.InteracaoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteracaoRepository extends JpaRepository<InteracaoModel, InteracaoId> {
    List<InteracaoModel> findByIdUsuarioIdAndCurtidoTrueOrderByDataAtualizacaoDesc(UUID usuarioId);
    List<InteracaoModel> findByIdUsuarioIdAndSalvoTrueOrderByDataAtualizacaoDesc(UUID usuarioId);
    long countByIdPostIdAndCurtidoTrue(UUID postId);
}
