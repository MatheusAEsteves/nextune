package com.nextune.artista.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/** Atualização parcial: campo null/ausente = mantém; texto vazio ("") = apaga o link. */
public record ArtistaUpdateRequest(
        @Size(max = 100) String nomeArtistico,
        @URL @Size(max = 255) String instagramUrl,
        @URL @Size(max = 255) String spotifyUrl,
        @URL @Size(max = 255) String youtubeUrl
) {}
