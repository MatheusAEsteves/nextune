package com.nextune.post.mapper;

import com.nextune.artista.mapper.ArtistaMapper;
import com.nextune.feed.repository.InteracaoRepository;
import com.nextune.genero.dto.GeneroResponse;
import com.nextune.genero.entity.GeneroModel;
import com.nextune.post.dto.PostResponse;
import com.nextune.post.entity.PostModel;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Converte PostModel em PostResponse. Use dentro de uma transação (acessa artista e gêneros lazy). */
@Component
public class PostMapper {

    private final InteracaoRepository interacaoRepository;

    public PostMapper(InteracaoRepository interacaoRepository) {
        this.interacaoRepository = interacaoRepository;
    }

    public PostResponse toResponse(PostModel p) {
        List<GeneroResponse> generos = p.getGeneros().stream()
                .sorted(Comparator.comparing(GeneroModel::getNome))
                .map(g -> new GeneroResponse(g.getId(), g.getNome()))
                .toList();

        return new PostResponse(
                p.getId(), p.getTitulo(), p.getDescricao(),
                p.getArquivoAudio(), p.getCapa(), p.getLinkMusica(),
                p.getDataPublicacao(),
                ArtistaMapper.toResumo(p.getArtista()),
                generos,
                interacaoRepository.countByIdPostIdAndCurtidoTrue(p.getId()));
    }

    public List<PostResponse> toResponseList(Collection<PostModel> posts) {
        return posts.stream().map(this::toResponse).toList();
    }
}
