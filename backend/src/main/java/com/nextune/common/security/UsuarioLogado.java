package com.nextune.common.security;

import com.nextune.usuario.entity.UsuarioModel;
import com.nextune.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Descobre quem é o usuário autenticado na requisição.
 * Usa authentication.getName(): aceita tanto o UUID do usuário quanto o e-mail
 * (subject do JWT ou username do login). Se o token de vocês guardar outra coisa,
 * basta ajustar o método id() — nenhum controller depende do formato.
 */
@Component
public class UsuarioLogado {

    private final UsuarioRepository usuarioRepository;

    public UsuarioLogado(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UUID id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw naoAutenticado();
        }
        String nome = auth.getName();
        if (nome == null || nome.isBlank()) {
            throw naoAutenticado();
        }
        try {
            return UUID.fromString(nome);
        } catch (IllegalArgumentException naoEhUuid) {
            return usuarioRepository.findByEmail(nome.trim().toLowerCase())
                    .map(UsuarioModel::getId)
                    .orElseThrow(UsuarioLogado::naoAutenticado);
        }
    }

    private static ResponseStatusException naoAutenticado() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
    }
}
