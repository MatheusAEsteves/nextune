package com.nextune.post.service;

import com.nextune.artista.entity.ArtistaModel;
import com.nextune.artista.repository.ArtistaRepository;
import com.nextune.common.security.PermissaoService;
import com.nextune.common.storage.ArmazenamentoService;
import com.nextune.common.validation.ValidacaoException;
import com.nextune.common.validation.Validacoes;
import com.nextune.common.web.PaginaResponse;
import com.nextune.common.web.Paginacao;
import com.nextune.genero.entity.GeneroModel;
import com.nextune.genero.repository.GeneroRepository;
import com.nextune.post.dto.PostResponse;
import com.nextune.post.dto.PostUpdateRequest;
import com.nextune.post.entity.PostModel;
import com.nextune.post.mapper.PostMapper;
import com.nextune.post.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PostService {

    private static final int MAX_GENEROS = 5;

    private final PostRepository postRepository;
    private final ArtistaRepository artistaRepository;
    private final GeneroRepository generoRepository;
    private final ArmazenamentoService armazenamento;
    private final PermissaoService permissaoService;
    private final PostMapper postMapper;

    public PostService(PostRepository postRepository,
                       ArtistaRepository artistaRepository,
                       GeneroRepository generoRepository,
                       ArmazenamentoService armazenamento,
                       PermissaoService permissaoService,
                       PostMapper postMapper) {
        this.postRepository = postRepository;
        this.artistaRepository = artistaRepository;
        this.generoRepository = generoRepository;
        this.armazenamento = armazenamento;
        this.permissaoService = permissaoService;
        this.postMapper = postMapper;
    }

    @Transactional
    public PostResponse criar(UUID usuarioId, String titulo, String descricao, String linkMusica,
                              List<Long> generoIds, MultipartFile audio, MultipartFile capa) {
        ArtistaModel artista = artistaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Ative seu perfil de artista para publicar músicas"));

        String tituloValido = Validacoes.textoObrigatorio(titulo, "titulo", 150);
        String descricaoValida = Validacoes.textoOpcional(descricao, "descricao", 5000);
        String linkValido = Validacoes.urlHttpObrigatoria(linkMusica, "linkMusica");
        Set<GeneroModel> generos = buscarGeneros(generoIds);

        String urlAudio = null;
        String urlCapa = null;
        try {
            urlAudio = armazenamento.salvar(audio, "audio",
                    ArmazenamentoService.EXT_AUDIO, ArmazenamentoService.LIMITE_AUDIO_BYTES);
            urlCapa = armazenamento.salvar(capa, "capa",
                    ArmazenamentoService.EXT_IMAGEM, ArmazenamentoService.LIMITE_IMAGEM_BYTES);

            PostModel post = new PostModel(artista, tituloValido, descricaoValida, urlAudio, urlCapa, linkValido);
            generos.forEach(post::addGenero);
            return postMapper.toResponse(postRepository.saveAndFlush(post));
        } catch (RuntimeException e) {
            // não deixa arquivos órfãos se algo falhar depois do upload
            armazenamento.remover(urlAudio);
            armazenamento.remover(urlCapa);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PostResponse buscar(UUID postId) {
        return postMapper.toResponse(buscarEntidade(postId));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PostResponse> listarPorArtista(UUID artistaId, int pagina, int tamanho) {
        if (!artistaRepository.existsById(artistaId)) {
            throw artistaNaoEncontrado();
        }
        Page<PostModel> page = postRepository.findByArtistaIdOrderByDataPublicacaoDesc(
                artistaId, Paginacao.de(pagina, tamanho));
        return PaginaResponse.de(page.map(postMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PostResponse> listarMeus(UUID usuarioId, int pagina, int tamanho) {
        ArtistaModel artista = artistaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(PostService::artistaNaoEncontrado);
        return listarPorArtista(artista.getId(), pagina, tamanho);
    }

    @Transactional
    public PostResponse atualizar(UUID usuarioId, UUID postId, PostUpdateRequest req) {
        PostModel post = buscarEntidade(postId);
        exigirDono(post, usuarioId);

        if (req.titulo() != null) {
            post.setTitulo(Validacoes.textoObrigatorio(req.titulo(), "titulo", 150));
        }
        if (req.descricao() != null) {
            post.setDescricao(Validacoes.textoOpcional(req.descricao(), "descricao", 5000));
        }
        if (req.linkMusica() != null) {
            post.setLinkMusica(Validacoes.urlHttpObrigatoria(req.linkMusica(), "linkMusica"));
        }
        if (req.generoIds() != null) {
            Set<GeneroModel> novos = buscarGeneros(req.generoIds());
            post.getGeneros().clear();
            novos.forEach(post::addGenero);
        }
        return postMapper.toResponse(post);
    }

    @Transactional
    public PostResponse atualizarCapa(UUID usuarioId, UUID postId, MultipartFile capa) {
        PostModel post = buscarEntidade(postId);
        exigirDono(post, usuarioId);

        String nova = armazenamento.salvar(capa, "capa",
                ArmazenamentoService.EXT_IMAGEM, ArmazenamentoService.LIMITE_IMAGEM_BYTES);
        String antiga = post.getCapa();
        post.setCapa(nova);
        armazenamento.remover(antiga);
        return postMapper.toResponse(post);
    }

    @Transactional
    public void remover(UUID usuarioId, UUID postId) {
        PostModel post = buscarEntidade(postId);
        if (!ehDono(post, usuarioId) && !permissaoService.ehAdmin(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode remover este post");
        }
        String audio = post.getArquivoAudio();
        String capa = post.getCapa();
        postRepository.delete(post);
        postRepository.flush();
        armazenamento.remover(audio);
        armazenamento.remover(capa);
    }

    private Set<GeneroModel> buscarGeneros(List<Long> generoIds) {
        Set<Long> unicos = new LinkedHashSet<>();
        if (generoIds != null) {
            unicos.addAll(generoIds);
            unicos.remove(null);
        }
        if (unicos.isEmpty()) {
            throw new ValidacaoException("generoIds", "Escolha ao menos um gênero");
        }
        if (unicos.size() > MAX_GENEROS) {
            throw new ValidacaoException("generoIds", "Escolha no máximo " + MAX_GENEROS + " gêneros");
        }
        List<GeneroModel> encontrados = generoRepository.findAllById(unicos);
        if (encontrados.size() != unicos.size()) {
            throw new ValidacaoException("generoIds", "Gênero inexistente");
        }
        return new HashSet<>(encontrados);
    }

    private PostModel buscarEntidade(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post não encontrado"));
    }

    private boolean ehDono(PostModel post, UUID usuarioId) {
        return post.getArtista().getUsuario().getId().equals(usuarioId);
    }

    private void exigirDono(PostModel post, UUID usuarioId) {
        if (!ehDono(post, usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este post pertence a outro artista");
        }
    }

    private static ResponseStatusException artistaNaoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado");
    }
}
