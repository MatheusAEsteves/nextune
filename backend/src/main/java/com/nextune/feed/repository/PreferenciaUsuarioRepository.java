package com.nextune.feed.repository;

import com.nextune.feed.entity.PreferenciaUsuarioModel;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = "genero")
    List<PreferenciaUsuarioModel> findByUsuarioIdOrderByPontuacaoDesc(UUID usuarioId);

    /**
     * Soma atômica (evita perder pontos em requisições simultâneas) e cria a linha se ela ainda não existe.
     * Os UUIDs vão como bytes (BINARY(16)) para não depender da conversão do driver em query nativa.
     * Use UuidBytes.de(...) para gerar os parâmetros "id" e "usuarioId".
     */
    @Modifying
    @Query(value = """
        INSERT INTO preferencia_usuario (id, usuario_id, genero_id, pontuacao, data_atualizacao)
        VALUES (:id, :usuarioId, :generoId, :pontos, NOW())
        ON DUPLICATE KEY UPDATE pontuacao = pontuacao + :pontos, data_atualizacao = NOW()
        """, nativeQuery = true)
    int somarPontosOuCriar(@Param("id") byte[] id,
                           @Param("usuarioId") byte[] usuarioId,
                           @Param("generoId") long generoId,
                           @Param("pontos") int pontos);
}
