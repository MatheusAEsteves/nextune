package com.nextune.feed.service;

import com.nextune.common.validation.ValidacaoException;
import com.nextune.feed.dto.PreferenciaResponse;
import com.nextune.feed.entity.PreferenciaUsuarioModel;
import com.nextune.feed.repository.PreferenciaUsuarioRepository;
import com.nextune.genero.entity.GeneroModel;
import com.nextune.genero.repository.GeneroRepository;
import com.nextune.usuario.entity.UsuarioModel;
import com.nextune.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PreferenciaService {

    /** Pontuação inicial de um gênero escolhido no onboarding (>0 para entrar nas recomendações). */
    public static final int PONTOS_ONBOARDING = 5;

    private final PreferenciaUsuarioRepository preferenciaRepository;
    private final GeneroRepository generoRepository;
    private final UsuarioRepository usuarioRepository;

    public PreferenciaService(PreferenciaUsuarioRepository preferenciaRepository,
                              GeneroRepository generoRepository,
                              UsuarioRepository usuarioRepository) {
        this.preferenciaRepository = preferenciaRepository;
        this.generoRepository = generoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<PreferenciaResponse> listar(UUID usuarioId) {
        return preferenciaRepository.findByUsuarioIdOrderByPontuacaoDesc(usuarioId).stream()
                .map(p -> new PreferenciaResponse(
                        p.getGenero().getId(), p.getGenero().getNome(), p.getPontuacao()))
                .toList();
    }

    /**
     * Onboarding: marca os gêneros escolhidos como favoritos. Não apaga o que o usuário já aprendeu
     * pelas curtidas e é seguro chamar mais de uma vez (não acumula pontos).
     */
    @Transactional
    public List<PreferenciaResponse> definirFavoritos(UUID usuarioId, List<Long> generoIds) {
        UsuarioModel usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Set<Long> ids = new LinkedHashSet<>(generoIds);
        ids.remove(null);
        List<GeneroModel> generos = generoRepository.findAllById(ids);
        if (ids.isEmpty() || generos.size() != ids.size()) {
            throw new ValidacaoException("generoIds", "Gênero inexistente");
        }

        for (GeneroModel genero : generos) {
            PreferenciaUsuarioModel preferencia = preferenciaRepository
                    .findByUsuarioIdAndGeneroId(usuarioId, genero.getId())
                    .orElseGet(() -> new PreferenciaUsuarioModel(usuario, genero, 0));
            preferencia.garantirPontuacaoMinima(PONTOS_ONBOARDING);
            preferenciaRepository.save(preferencia);
        }
        return listar(usuarioId);
    }
}
