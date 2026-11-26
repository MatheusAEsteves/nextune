package com.nextune.usuario.dto;

import jakarta.validation.constraints.Size;

/**
 * Atualização parcial: campo null/ausente = mantém o valor atual; texto vazio ("") = apaga (bio, fotoPerfil).
 * fotoPerfil só aceita URL http(s); para enviar imagem use POST /usuarios/me/foto.
 */
public record UsuarioUpdateRequest(@Size(max = 150) String nome,
                                   @Size(max = 2000) String bio,
                                   @Size(max = 255) String fotoPerfil) {
}
