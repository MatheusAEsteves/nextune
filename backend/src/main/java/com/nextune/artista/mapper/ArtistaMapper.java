package com.nextune.artista.mapper;

import com.nextune.artista.dto.ArtistaResponse;
import com.nextune.artista.dto.ArtistaResumoResponse;
import com.nextune.artista.entity.ArtistaModel;

public final class ArtistaMapper {

    private ArtistaMapper() {}

    public static ArtistaResponse toResponse(ArtistaModel a) {
        return new ArtistaResponse(
                a.getId(), a.getUsuario().getId(), a.getNomeArtistico(),
                a.getInstagramUrl(), a.getSpotifyUrl(), a.getYoutubeUrl());
    }

    public static ArtistaResumoResponse toResumo(ArtistaModel a) {
        return new ArtistaResumoResponse(a.getId(), a.getNomeArtistico());
    }
}
