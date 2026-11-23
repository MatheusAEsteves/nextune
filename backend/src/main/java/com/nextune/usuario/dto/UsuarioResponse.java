package com.nextune.usuario.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        String fotoPerfil,
        String bio,
        LocalDate dataCriacao
) {}
