package com.nextune.seguidor.dto;

/** total = quantos usuários seguem o artista; seguindo = se o usuário logado já o segue. */
public record SeguidoresResponse(long total, boolean seguindo) {}
