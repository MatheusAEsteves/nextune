package com.nextune.artista.service;

import com.nextune.artista.dto.ArtistaRequest;
import com.nextune.artista.dto.ArtistaResponse;
import com.nextune.artista.entity.ArtistaModel;
import com.nextune.artista.mapper.ArtistaMapper;
import com.nextune.artista.repository.ArtistaRepository;
import com.nextune.usuario.entity.UsuarioModel;
import com.nextune.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ArtistaService {

    private final ArtistaRepository artistaRepository;
    private final UsuarioRepository usuarioRepository;

    public ArtistaService(ArtistaRepository artistaRepository, UsuarioRepository usuarioRepository) {
        this.artistaRepository = artistaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ArtistaResponse ativar(UUID usuarioId, ArtistaRequest req) {
        UsuarioModel usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (artistaRepository.existsByUsuarioId(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já é artista");
        }

        ArtistaModel artista = new ArtistaModel(
                usuario, req.nomeArtistico().trim(),
                req.instagramUrl(), req.spotifyUrl(), req.youtubeUrl());

        return ArtistaMapper.toResponse(artistaRepository.save(artista));
    }

    @Transactional(readOnly = true)
    public ArtistaResponse buscarPorId(UUID id) {
        return ArtistaMapper.toResponse(artistaRepository.findById(id)
                .orElseThrow(() -> naoEncontrado()));
    }

    @Transactional(readOnly = true)
    public ArtistaResponse buscarPorUsuario(UUID usuarioId) {
        return ArtistaMapper.toResponse(artistaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> naoEncontrado()));
    }

    @Transactional
    public ArtistaResponse atualizar(UUID usuarioId, ArtistaRequest req) {
        ArtistaModel artista = artistaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> naoEncontrado());

        artista.setNomeArtistico(req.nomeArtistico().trim());
        artista.setInstagramUrl(req.instagramUrl());
        artista.setSpotifyUrl(req.spotifyUrl());
        artista.setYoutubeUrl(req.youtubeUrl());

        return ArtistaMapper.toResponse(artista);
    }

    private ResponseStatusException naoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado");
    }
}
