package com.nextune.denuncia.dto;

import com.nextune.denuncia.entity.DenunciaStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DenunciaResponse(
        UUID id,
        UUID usuarioId,
        UUID postId,
        UUID artistaId,
        String motivo,
        String descricao,
        DenunciaStatus status,
        LocalDateTime dataDenuncia) {}
