package com.nextune.seguidor.controller;

import com.nextune.artista.dto.ArtistaResponse;
import com.nextune.common.security.UsuarioLogado;
import com.nextune.common.web.PaginaResponse;
import com.nextune.seguidor.dto.SeguidoresResponse;
import com.nextune.seguidor.service.SeguidorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class SeguidorController {

    private final SeguidorService seguidorService;
    private final UsuarioLogado usuarioLogado;

    public SeguidorController(SeguidorService seguidorService, UsuarioLogado usuarioLogado) {
        this.seguidorService = seguidorService;
        this.usuarioLogado = usuarioLogado;
    }

    @PutMapping("/artistas/{artistaId}/seguir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void seguir(@PathVariable UUID artistaId) {
        seguidorService.seguir(usuarioLogado.id(), artistaId);
    }

    @DeleteMapping("/artistas/{artistaId}/seguir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deixarDeSeguir(@PathVariable UUID artistaId) {
        seguidorService.deixarDeSeguir(usuarioLogado.id(), artistaId);
    }

    /** Total de seguidores do artista e se o usuário logado já o segue. */
    @GetMapping("/artistas/{artistaId}/seguidores")
    public SeguidoresResponse seguidores(@PathVariable UUID artistaId) {
        return seguidorService.resumo(usuarioLogado.id(), artistaId);
    }

    /** Artistas que o usuário logado segue. */
    @GetMapping("/usuarios/me/seguindo")
    public PaginaResponse<ArtistaResponse> seguindo(@RequestParam(defaultValue = "0") int pagina,
                                                    @RequestParam(defaultValue = "20") int tamanho) {
        return seguidorService.listarSeguindo(usuarioLogado.id(), pagina, tamanho);
    }
}
