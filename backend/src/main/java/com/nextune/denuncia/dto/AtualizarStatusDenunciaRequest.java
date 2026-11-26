package com.nextune.denuncia.dto;

import com.nextune.denuncia.entity.DenunciaStatus;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusDenunciaRequest(@NotNull DenunciaStatus status) {}
