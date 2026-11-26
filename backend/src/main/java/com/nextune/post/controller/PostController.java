package com.nextune.post.controller;

import com.nextune.common.security.UsuarioLogado;
import com.nextune.common.web.PaginaResponse;
import com.nextune.post.dto.PostResponse;
import com.nextune.post.dto.PostUpdateRequest;
import com.nextune.post.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class PostController {

    private final PostService postService;
    private final UsuarioLogado usuarioLogado;

    public PostController(PostService postService, UsuarioLogado usuarioLogado) {
        this.postService = postService;
        this.usuarioLogado = usuarioLogado;
    }

    /**
     * Publica uma música (somente artistas). Envio como multipart/form-data:
     * titulo, descricao (opcional), linkMusica, generoIds (repetido), audio (arquivo), capa (arquivo).
     */
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse criar(@RequestParam String titulo,
                              @RequestParam(required = false) String descricao,
                              @RequestParam String linkMusica,
                              @RequestParam List<Long> generoIds,
                              @RequestParam("audio") MultipartFile audio,
                              @RequestParam("capa") MultipartFile capa) {
        return postService.criar(usuarioLogado.id(), titulo, descricao, linkMusica, generoIds, audio, capa);
    }

    @GetMapping("/posts/{id}")
    public PostResponse buscar(@PathVariable UUID id) {
        return postService.buscar(id);
    }

    @PutMapping("/posts/{id}")
    public PostResponse atualizar(@PathVariable UUID id, @Valid @RequestBody PostUpdateRequest req) {
        return postService.atualizar(usuarioLogado.id(), id, req);
    }

    /** Troca a capa (multipart, campo "capa"). */
    @PutMapping(value = "/posts/{id}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostResponse atualizarCapa(@PathVariable UUID id, @RequestParam("capa") MultipartFile capa) {
        return postService.atualizarCapa(usuarioLogado.id(), id, capa);
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        postService.remover(usuarioLogado.id(), id);
    }

    /** Posts do artista logado, do mais novo para o mais antigo. */
    @GetMapping("/artistas/me/posts")
    public PaginaResponse<PostResponse> meusPosts(@RequestParam(defaultValue = "0") int pagina,
                                                  @RequestParam(defaultValue = "20") int tamanho) {
        return postService.listarMeus(usuarioLogado.id(), pagina, tamanho);
    }

    /** Posts de um artista, do mais novo para o mais antigo. */
    @GetMapping("/artistas/{artistaId}/posts")
    public PaginaResponse<PostResponse> postsDoArtista(@PathVariable UUID artistaId,
                                                       @RequestParam(defaultValue = "0") int pagina,
                                                       @RequestParam(defaultValue = "20") int tamanho) {
        return postService.listarPorArtista(artistaId, pagina, tamanho);
    }
}
