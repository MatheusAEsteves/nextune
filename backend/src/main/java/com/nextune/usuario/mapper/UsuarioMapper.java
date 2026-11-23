package com.nextune.usuario.mapper;

import com.nextune.usuario.dto.UsuarioResponse;
import com.nextune.usuario.entity.UsuarioModel;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public static UsuarioResponse toResponse(UsuarioModel u) {
        return new UsuarioResponse(
                u.getId(), u.getNome(), u.getEmail(),
                u.getFotoPerfil(), u.getBio(), u.getDataCriacao());
    }

}
