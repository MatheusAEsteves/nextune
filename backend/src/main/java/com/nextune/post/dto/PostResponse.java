package com.nextune.post.dto;

import com.nextune.artista.dto.ArtistaResumoResponse;
import com.nextune.genero.dto.GeneroResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** arquivoAudio e capa são caminhos públicos (ex.: /uploads/audio/x.mp3): o front prefixa a URL da API. */
public record PostResponse(
        UUID id,
        String titulo,
        String descricao,
        String arquivoAudio,
        String capa,
        String linkMusica,
        LocalDateTime dataPublicacao,
        ArtistaResumoResponse artista,
        List<GeneroResponse> generos,
        long totalCurtidas) {}
