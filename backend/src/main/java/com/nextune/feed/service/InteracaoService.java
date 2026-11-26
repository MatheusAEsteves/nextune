package com.nextune.feed.service;

import com.nextune.common.util.UuidBytes;
import com.nextune.common.validation.ValidacaoException;
import com.nextune.common.web.PaginaResponse;
import com.nextune.common.web.Paginacao;
import com.nextune.feed.dto.InteracaoRequest;
import com.nextune.feed.dto.InteracaoResponse;
import com.nextune.feed.entity.InteracaoId;
import com.nextune.feed.entity.InteracaoModel;
import com.nextune.feed.repository.InteracaoRepository;
import com.nextune.feed.repository.PreferenciaUsuarioRepository;
import com.nextune.genero.entity.GeneroModel;
import com.nextune.post.dto.PostResponse;
import com.nextune.post.entity.PostModel;
import com.nextune.post.mapper.PostMapper;
import com.nextune.post.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InteracaoService {

    private final InteracaoRepository interacaoRepository;
    private final PreferenciaUsuarioRepository preferenciaRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public InteracaoService(InteracaoRepository interacaoRepository,
                            PreferenciaUsuarioRepository preferenciaRepository,
                            PostRepository postRepository,
                            PostMapper postMapper) {
        this.interacaoRepository = interacaoRepository;
        this.preferenciaRepository = preferenciaRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
    }

    /**
     * Cria ou atualiza a interação do usuário com um post e ajusta a pontuação dos gêneros do post
     * pela diferença entre o estado antigo e o novo (curtir +3, salvar +2, pular -1; desfazer devolve os pontos).
     * Uma chamada só com postId registra o post como "visto" (ele deixa de aparecer no feed).
     */
    @Transactional
    public InteracaoResponse registrar(UUID usuarioId, InteracaoRequest req) {
        PostModel post = postRepository.findById(req.postId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post não encontrado"));

        if (post.getArtista().getUsuario().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode interagir com o seu próprio post");
        }
        if (Boolean.TRUE.equals(req.curtido()) && Boolean.TRUE.equals(req.pulou())) {
            throw new ValidacaoException("pulou", "Um post não pode ser curtido e pulado ao mesmo tempo");
        }

        InteracaoModel interacao = interacaoRepository.findById(new InteracaoId(usuarioId, post.getId()))
                .orElseGet(() -> new InteracaoModel(usuarioId, post.getId()));
        int pontosAntes = interacao.pontos();

        if (req.curtido() != null) {
            interacao.setCurtido(req.curtido());
            if (req.curtido()) {
                interacao.setPulou(false);
            }
        }
        if (req.pulou() != null) {
            interacao.setPulou(req.pulou());
            if (req.pulou()) {
                interacao.setCurtido(false);
            }
        }
        if (req.salvo() != null) {
            interacao.setSalvo(req.salvo());
        }
        if (req.segundosOuvidos() != null) {
            // o tempo ouvido só cresce
            interacao.setSegundosOuvidos(Math.max(interacao.getSegundosOuvidos(), req.segundosOuvidos()));
        }

        interacaoRepository.save(interacao);

        int variacao = interacao.pontos() - pontosAntes;
        if (variacao != 0) {
            for (GeneroModel genero : post.getGeneros()) {
                preferenciaRepository.somarPontosOuCriar(
                        UuidBytes.de(UUID.randomUUID()), UuidBytes.de(usuarioId), genero.getId(), variacao);
            }
        }

        return new InteracaoResponse(post.getId(), interacao.isCurtido(), interacao.isSalvo(),
                interacao.isPulou(), interacao.getSegundosOuvidos());
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PostResponse> listarCurtidas(UUID usuarioId, int pagina, int tamanho) {
        return paraPosts(interacaoRepository
                .findByIdUsuarioIdAndCurtidoTrueOrderByDataAtualizacaoDesc(usuarioId, Paginacao.de(pagina, tamanho)));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PostResponse> listarSalvos(UUID usuarioId, int pagina, int tamanho) {
        return paraPosts(interacaoRepository
                .findByIdUsuarioIdAndSalvoTrueOrderByDataAtualizacaoDesc(usuarioId, Paginacao.de(pagina, tamanho)));
    }

    private PaginaResponse<PostResponse> paraPosts(Page<InteracaoModel> pagina) {
        List<UUID> ids = pagina.getContent().stream().map(i -> i.getId().getPostId()).toList();
        Map<UUID, PostModel> postsPorId = postRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PostModel::getId, Function.identity()));

        // mantém a ordem da interação mais recente para a mais antiga
        List<PostResponse> conteudo = ids.stream()
                .map(postsPorId::get)
                .filter(Objects::nonNull)
                .map(postMapper::toResponse)
                .toList();

        return new PaginaResponse<>(conteudo, pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }
}
