package com.nextune.artista.dto;

import java.util.UUID;

/** Dados mínimos do artista para listas (feed, posts). */
public record ArtistaResumoResponse(UUID id, String nomeArtistico) {}
