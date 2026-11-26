package com.nextune.comentario.controller;

import com.nextune.comentario.dto.ComentarioRequest;
import com.nextune.comentario.dto.ComentarioResponse;
import com.nextune.comentario.service.ComentarioService;
import com.nextune.common.security.UsuarioLogado;
import com.nextune.common.web.PaginaResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final UsuarioLogado usuarioLogado;

    public ComentarioController(ComentarioService comentarioService, UsuarioLogado usuarioLogado) {
        this.comentarioService = comentarioService;
        this.usuarioLogado = usuarioLogado;
    }

    @PostMapping("/posts/{postId}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponse criar(@PathVariable UUID postId, @Valid @RequestBody ComentarioRequest req) {
        return comentarioService.criar(usuarioLogado.id(), postId, req);
    }

    @GetMapping("/posts/{postId}/comentarios")
    public PaginaResponse<ComentarioResponse> listar(@PathVariable UUID postId,
                                                     @RequestParam(defaultValue = "0") int pagina,
                                                     @RequestParam(defaultValue = "20") int tamanho) {
        return comentarioService.listar(postId, pagina, tamanho);
    }

    @DeleteMapping("/comentarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        comentarioService.remover(usuarioLogado.id(), id);
    }
}
