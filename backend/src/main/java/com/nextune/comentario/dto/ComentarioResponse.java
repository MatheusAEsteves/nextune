package com.nextune.comentario.dto;

import com.nextune.usuario.dto.AutorResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record ComentarioResponse(
        UUID id,
        UUID postId,
        AutorResponse autor,
        String conteudo,
        LocalDateTime dataComentario) {}
