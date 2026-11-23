package com.nextune.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(@NotBlank @Size(max = 150) String nome,
                                   @Size(max = 2000) String bio,
                                   @Size(max = 255) String fotoPerfil) {
}
