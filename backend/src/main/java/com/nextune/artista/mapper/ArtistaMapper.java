package com.nextune.artista.mapper;

import com.nextune.artista.dto.ArtistaResponse;
import com.nextune.artista.entity.ArtistaModel;
import org.springframework.stereotype.Component;

@Component
public class ArtistaMapper {

    public static ArtistaResponse toResponse(ArtistaModel a) {
        return new ArtistaResponse(
                a.getId(), a.getUsuario().getId(), a.getNomeArtistico(),
                a.getInstagramUrl(), a.getSpotifyUrl(), a.getYoutubeUrl());
    }

}
