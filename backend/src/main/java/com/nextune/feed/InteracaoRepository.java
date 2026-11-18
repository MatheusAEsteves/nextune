package com.nextune.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteracaoRepository extends JpaRepository<InteracaoModel, InteracaoId> {
    List<InteracaoModel> findByUsuarioIdAndCurtidoTrueOrderByDataAtualizacaoDesc(UUID usuarioId);
    List<InteracaoModel> findByUsuarioIdAndSalvoTrueOrderByDataAtualizacaoDesc(UUID usuarioId);
    long countByIdPostIdAndCurtidoTrue(UUID postId);
}
