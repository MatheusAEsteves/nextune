package com.nextune.post.repository;

import com.nextune.post.entity.PostModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<PostModel, UUID> {
    Page<PostModel> findByArtistaIdOrderByDataPublicacaoDesc(UUID artistaId, Pageable pageable);

    @Query(value = """
        SELECT p.*
        FROM post p
        JOIN post_has_genero pg ON pg.post_id = p.id
        JOIN preferencia_usuario pu
             ON pu.genero_id = pg.genero_id AND pu.usuario_id = :usuarioId
        WHERE pu.pontuacao > 0
          AND NOT EXISTS (
              SELECT 1 FROM interacao i
              WHERE i.post_id = p.id AND i.usuario_id = :usuarioId)
          AND p.artista_id NOT IN (
              SELECT a.id FROM artista a WHERE a.usuario_id = :usuarioId)
        GROUP BY p.id
        ORDER BY MAX(pu.pontuacao) DESC, RAND()
        LIMIT :limite
        """, nativeQuery = true)
    List<PostModel> findRecomendados(@Param("usuarioId") UUID usuarioId,
                                     @Param("limite") int limite);

    @Query(value = """
        SELECT p.*
        FROM post p
        WHERE NOT EXISTS (
              SELECT 1 FROM interacao i
              WHERE i.post_id = p.id AND i.usuario_id = :usuarioId)
          AND p.artista_id NOT IN (
              SELECT a.id FROM artista a WHERE a.usuario_id = :usuarioId)
          AND p.id NOT IN (:excluidos)
        ORDER BY RAND()
        LIMIT :limite
        """, nativeQuery = true)
    List<PostModel> findAleatorios(@Param("usuarioId") UUID usuarioId,
                                   @Param("excluidos") Collection<UUID> excluidos,
                                   @Param("limite") int limite);
}
