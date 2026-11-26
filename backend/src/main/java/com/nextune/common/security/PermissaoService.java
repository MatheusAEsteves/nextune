package com.nextune.common.security;

import com.nextune.usuario.entity.RoleUsuario;
import com.nextune.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/** Checagem de papel (ADMIN) feita direto no banco, independente de como o token carrega as roles. */
@Service
public class PermissaoService {

    private final UsuarioRepository usuarioRepository;

    public PermissaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public boolean ehAdmin(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(u -> u.getRole() == RoleUsuario.ADMIN)
                .orElse(false);
    }

    public void exigirAdmin(UUID usuarioId) {
        if (!ehAdmin(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a administradores");
        }
    }
}
