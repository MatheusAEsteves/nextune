package com.nextune.usuario.mapper;

import com.nextune.usuario.dto.AutorResponse;
import com.nextune.usuario.dto.UsuarioPublicoResponse;
import com.nextune.usuario.dto.UsuarioResponse;
import com.nextune.usuario.entity.UsuarioModel;

public final class UsuarioMapper {

    private UsuarioMapper() {}

    public static UsuarioResponse toResponse(UsuarioModel u) {
        return new UsuarioResponse(
                u.getId(), u.getNome(), u.getEmail(),
                u.getFotoPerfil(), u.getBio(), u.getDataCriacao());
    }

    public static UsuarioPublicoResponse toPublico(UsuarioModel u) {
        return new UsuarioPublicoResponse(
                u.getId(), u.getNome(), u.getFotoPerfil(), u.getBio(), u.getDataCriacao());
    }

    public static AutorResponse toAutor(UsuarioModel u) {
        return new AutorResponse(u.getId(), u.getNome(), u.getFotoPerfil());
    }
}
