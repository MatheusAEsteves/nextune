package com.nextune.usuario.dto;

import java.util.UUID;

/** Dados mínimos de quem escreveu algo (ex.: comentário). */
public record AutorResponse(UUID id, String nome, String fotoPerfil) {}
