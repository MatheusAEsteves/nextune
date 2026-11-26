package com.nextune.feed.controller;

import com.nextune.common.security.UsuarioLogado;
import com.nextune.feed.dto.DefinirPreferenciasRequest;
import com.nextune.feed.dto.PreferenciaResponse;
import com.nextune.feed.service.PreferenciaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/preferencias")
public class PreferenciaController {

    private final PreferenciaService preferenciaService;
    private final UsuarioLogado usuarioLogado;

    public PreferenciaController(PreferenciaService preferenciaService, UsuarioLogado usuarioLogado) {
        this.preferenciaService = preferenciaService;
        this.usuarioLogado = usuarioLogado;
    }

    /** Gêneros do usuário logado, do que mais gosta para o que menos gosta. Lista vazia = precisa do onboarding. */
    @GetMapping("/me")
    public List<PreferenciaResponse> minhasPreferencias() {
        return preferenciaService.listar(usuarioLogado.id());
    }

    /** Onboarding: define os gêneros favoritos. */
    @PutMapping("/me")
    public List<PreferenciaResponse> definir(@Valid @RequestBody DefinirPreferenciasRequest req) {
        return preferenciaService.definirFavoritos(usuarioLogado.id(), req.generoIds());
    }
}
