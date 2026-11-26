package com.nextune.denuncia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Informe exatamente um alvo: postId OU artistaId. */
public record DenunciaRequest(
        UUID postId,
        UUID artistaId,
        @NotBlank @Size(max = 100) String motivo,
        @Size(max = 2000) String descricao) {}
