package com.nextune.usuario.dto;

import java.time.LocalDate;
import java.util.UUID;

/** Perfil visível para qualquer usuário: sem e-mail. */
public record UsuarioPublicoResponse(
        UUID id,
        String nome,
        String fotoPerfil,
        String bio,
        LocalDate dataCriacao
) {}
