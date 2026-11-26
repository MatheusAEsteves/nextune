package com.nextune.feed.controller;

import com.nextune.common.security.UsuarioLogado;
import com.nextune.common.web.PaginaResponse;
import com.nextune.feed.dto.InteracaoRequest;
import com.nextune.feed.dto.InteracaoResponse;
import com.nextune.feed.service.InteracaoService;
import com.nextune.post.dto.PostResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interacoes")
public class InteracaoController {

    private final InteracaoService interacaoService;
    private final UsuarioLogado usuarioLogado;

    public InteracaoController(InteracaoService interacaoService, UsuarioLogado usuarioLogado) {
        this.interacaoService = interacaoService;
        this.usuarioLogado = usuarioLogado;
    }

    /** Registra curtir / pular / salvar (cria ou atualiza). Chame uma vez por card do swipe. */
    @PostMapping
    public InteracaoResponse registrar(@Valid @RequestBody InteracaoRequest req) {
        return interacaoService.registrar(usuarioLogado.id(), req);
    }

    /** Músicas curtidas pelo usuário logado (mais recentes primeiro). */
    @GetMapping("/curtidas")
    public PaginaResponse<PostResponse> curtidas(@RequestParam(defaultValue = "0") int pagina,
                                                 @RequestParam(defaultValue = "20") int tamanho) {
        return interacaoService.listarCurtidas(usuarioLogado.id(), pagina, tamanho);
    }

    /** Músicas salvas pelo usuário logado (mais recentes primeiro). */
    @GetMapping("/salvos")
    public PaginaResponse<PostResponse> salvos(@RequestParam(defaultValue = "0") int pagina,
                                               @RequestParam(defaultValue = "20") int tamanho) {
        return interacaoService.listarSalvos(usuarioLogado.id(), pagina, tamanho);
    }
}
