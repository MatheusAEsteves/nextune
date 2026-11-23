package com.nextune.artista.dto;

import java.util.UUID;

public record ArtistaResponse(
        UUID id,
        UUID usuarioId,
        String nomeArtistico,
        String instagramUrl,
        String spotifyUrl,
        String youtubeUrl) {}
