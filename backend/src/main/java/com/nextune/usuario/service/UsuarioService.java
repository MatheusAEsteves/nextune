package com.nextune.usuario.service;

import com.nextune.usuario.dto.UsuarioCreateRequest;
import com.nextune.usuario.dto.UsuarioResponse;
import com.nextune.usuario.dto.UsuarioUpdateRequest;
import com.nextune.usuario.entity.UsuarioModel;
import com.nextune.usuario.mapper.UsuarioMapper;
import com.nextune.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCreateRequest req) {
        String email = req.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        UsuarioModel usuario = new UsuarioModel(
                req.nome().trim(), email, passwordEncoder.encode(req.senha()));

        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id) {
        return UsuarioMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponse atualizar(UUID id, UsuarioUpdateRequest req) {
        UsuarioModel usuario = buscarEntidade(id);
        usuario.setNome(req.nome().trim());
        usuario.setBio(req.bio());
        usuario.setFotoPerfil(req.fotoPerfil());
        return UsuarioMapper.toResponse(usuario);
    }

    private UsuarioModel buscarEntidade(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}
