package com.nextune.feed.service;

import com.nextune.post.dto.PostResponse;
import com.nextune.post.entity.PostModel;
import com.nextune.post.mapper.PostMapper;
import com.nextune.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class FeedService {

    public static final int LIMITE_MAXIMO = 20;
    private static final int MAX_EXCLUIDOS = 200;

    // NOT IN () vazio é erro de SQL; este UUID nunca existe e mantém a lista sempre preenchida.
    private static final UUID UUID_NULO = new UUID(0L, 0L);

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public FeedService(PostRepository postRepository, PostMapper postMapper) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
    }

    /**
     * Monta o lote de cards do swipe: primeiro o que combina com os gêneros do usuário;
     * se não houver o bastante (ex.: usuário novo), completa com posts aleatórios.
     * "excluir" são os posts que o front já carregou e ainda não registrou como interação.
     */
    @Transactional(readOnly = true)
    public List<PostResponse> montar(UUID usuarioId, int limite, Collection<UUID> excluir) {
        int quantidade = Math.min(Math.max(limite, 1), LIMITE_MAXIMO);

        Set<UUID> excluidos = new LinkedHashSet<>();
        excluidos.add(UUID_NULO);
        if (excluir != null) {
            excluir.stream().filter(Objects::nonNull).limit(MAX_EXCLUIDOS).forEach(excluidos::add);
        }

        List<PostModel> posts = new ArrayList<>(postRepository.findRecomendados(usuarioId, excluidos, quantidade));
        if (posts.size() < quantidade) {
            posts.forEach(p -> excluidos.add(p.getId()));
            posts.addAll(postRepository.findAleatorios(usuarioId, excluidos, quantidade - posts.size()));
        }
        return postMapper.toResponseList(posts);
    }
}
