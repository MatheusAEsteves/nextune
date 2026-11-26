package com.nextune.feed.controller;

import com.nextune.common.security.UsuarioLogado;
import com.nextune.feed.service.FeedService;
import com.nextune.post.dto.PostResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class FeedController {

    private final FeedService feedService;
    private final UsuarioLogado usuarioLogado;

    public FeedController(FeedService feedService, UsuarioLogado usuarioLogado) {
        this.feedService = feedService;
        this.usuarioLogado = usuarioLogado;
    }

    /**
     * Próximos cards do swipe. Já exclui posts com os quais o usuário interagiu e os próprios posts.
     * Envie em "excluir" os ids que o front já tem na tela/fila (ex.: ?excluir=id1,id2) para não repetir cards.
     */
    @GetMapping("/feed")
    public List<PostResponse> feed(@RequestParam(defaultValue = "10") int limite,
                                   @RequestParam(required = false) List<UUID> excluir) {
        return feedService.montar(usuarioLogado.id(), limite, excluir);
    }
}
