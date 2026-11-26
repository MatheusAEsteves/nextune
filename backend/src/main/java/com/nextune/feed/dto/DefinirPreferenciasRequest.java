package com.nextune.feed.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Gêneros escolhidos no onboarding (ids vindos de GET /generos). */
public record DefinirPreferenciasRequest(
        @NotEmpty @Size(max = 20) List<@NotNull Long> generoIds) {}
