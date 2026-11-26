package com.nextune.post.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

/** Atualização parcial: campo null/ausente = mantém. descricao "" = apaga. generoIds substitui a lista inteira. */
public record PostUpdateRequest(
        @Size(max = 150) String titulo,
        @Size(max = 5000) String descricao,
        @Size(max = 255) String linkMusica,
        @Size(max = 5) List<Long> generoIds) {}
