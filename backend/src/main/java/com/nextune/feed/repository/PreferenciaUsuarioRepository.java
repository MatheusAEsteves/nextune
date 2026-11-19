package com.nextune.feed.repository;

import com.nextune.feed.entity.PreferenciaUsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PreferenciaUsuarioRepository extends JpaRepository<PreferenciaUsuarioModel, UUID> {
    Optional<PreferenciaUsuarioModel> findByUsuarioIdAndGeneroId(UUID usuarioId, Long generoId);
    List<PreferenciaUsuarioModel> findByUsuarioIdOrderByPontuacaoDesc(UUID usuarioId);

    // Soma atômica (evita perder pontos em requisições simultâneas)
    @Modifying
    @Query("""
        UPDATE PreferenciaUsuarioModel p
        SET p.pontuacao = p.pontuacao + :pontos
        WHERE p.usuario.id = :usuarioId AND p.genero.id = :generoId
        """)
    int somarPontos(@Param("usuarioId") UUID usuarioId,
                    @Param("generoId") Long generoId,
                    @Param("pontos") int pontos);

}
