package com.nextune.comentario.mapper;

import com.nextune.comentario.dto.ComentarioResponse;
import com.nextune.comentario.entity.ComentarioModel;
import com.nextune.usuario.mapper.UsuarioMapper;

public final class ComentarioMapper {

    private ComentarioMapper() {}

    public static ComentarioResponse toResponse(ComentarioModel c) {
        return new ComentarioResponse(
                c.getId(), c.getPost().getId(), UsuarioMapper.toAutor(c.getUsuario()),
                c.getConteudo(), c.getDataComentario());
    }
}
