package com.nextune.seguidor.service;

import com.nextune.artista.dto.ArtistaResponse;
import com.nextune.artista.entity.ArtistaModel;
import com.nextune.artista.mapper.ArtistaMapper;
import com.nextune.artista.repository.ArtistaRepository;
import com.nextune.common.web.PaginaResponse;
import com.nextune.common.web.Paginacao;
import com.nextune.seguidor.dto.SeguidoresResponse;
import com.nextune.seguidor.entity.SeguidorModel;
import com.nextune.seguidor.repository.SeguidorRepository;
import com.nextune.usuario.entity.UsuarioModel;
import com.nextune.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class SeguidorService {

    private final SeguidorRepository seguidorRepository;
    private final ArtistaRepository artistaRepository;
    private final UsuarioRepository usuarioRepository;

    public SeguidorService(SeguidorRepository seguidorRepository,
                           ArtistaRepository artistaRepository,
                           UsuarioRepository usuarioRepository) {
        this.seguidorRepository = seguidorRepository;
        this.artistaRepository = artistaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Idempotente: seguir quem já se segue não gera erro. */
    @Transactional
    public void seguir(UUID usuarioId, UUID artistaId) {
        ArtistaModel artista = buscarArtista(artistaId);
        if (artista.getUsuario().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode seguir a si mesmo");
        }
        if (seguidorRepository.existsByUsuarioIdAndArtistaId(usuarioId, artistaId)) {
            return;
        }
        UsuarioModel usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        seguidorRepository.save(new SeguidorModel(usuario, artista));
    }

    /** Idempotente: deixar de seguir quem não se segue não gera erro. */
    @Transactional
    public void deixarDeSeguir(UUID usuarioId, UUID artistaId) {
        seguidorRepository.deleteByUsuarioIdAndArtistaId(usuarioId, artistaId);
    }

    @Transactional(readOnly = true)
    public SeguidoresResponse resumo(UUID usuarioId, UUID artistaId) {
        if (!artistaRepository.existsById(artistaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado");
        }
        return new SeguidoresResponse(
                seguidorRepository.countByArtistaId(artistaId),
                seguidorRepository.existsByUsuarioIdAndArtistaId(usuarioId, artistaId));
    }

    /** Artistas que o usuário segue (os seguidos mais recentemente primeiro). */
    @Transactional(readOnly = true)
    public PaginaResponse<ArtistaResponse> listarSeguindo(UUID usuarioId, int pagina, int tamanho) {
        return PaginaResponse.de(
                seguidorRepository.findByUsuarioIdOrderByDataSeguimentoDesc(usuarioId, Paginacao.de(pagina, tamanho))
                        .map(s -> ArtistaMapper.toResponse(s.getArtista())));
    }

    private ArtistaModel buscarArtista(UUID artistaId) {
        return artistaRepository.findById(artistaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));
    }
}
