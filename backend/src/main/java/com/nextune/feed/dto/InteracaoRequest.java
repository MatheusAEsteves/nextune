package com.nextune.feed.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Estado desejado da interação. Campo null/ausente = não altera.
 * Swipe para a direita: {postId, curtido: true}. Swipe para a esquerda: {postId, pulou: true}.
 * Salvar: {postId, salvo: true}. Desfazer: mande o campo com false.
 */
public record InteracaoRequest(
        @NotNull UUID postId,
        Boolean curtido,
        Boolean salvo,
        Boolean pulou,
        @Min(0) @Max(86400) Integer segundosOuvidos) {}
