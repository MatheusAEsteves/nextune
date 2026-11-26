package com.nextune.artista.controller;

import com.nextune.artista.dto.ArtistaRequest;
import com.nextune.artista.dto.ArtistaResponse;
import com.nextune.artista.dto.ArtistaUpdateRequest;
import com.nextune.artista.service.ArtistaService;
import com.nextune.common.security.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/artistas")
public class ArtistaController {

    private final ArtistaService artistaService;
    private final UsuarioLogado usuarioLogado;

    public ArtistaController(ArtistaService artistaService, UsuarioLogado usuarioLogado) {
        this.artistaService = artistaService;
        this.usuarioLogado = usuarioLogado;
    }

    /** Transforma o usuário logado em artista. */
    @PostMapping("/ativar")
    @ResponseStatus(HttpStatus.CREATED)
    public ArtistaResponse ativar(@Valid @RequestBody ArtistaRequest req) {
        return artistaService.ativar(usuarioLogado.id(), req);
    }

    /** Perfil de artista do usuário logado. Devolve 404 se ele ainda não é artista. */
    @GetMapping("/me")
    public ArtistaResponse meuPerfil() {
        return artistaService.buscarPorUsuario(usuarioLogado.id());
    }

    @PutMapping("/me")
    public ArtistaResponse atualizar(@Valid @RequestBody ArtistaUpdateRequest req) {
        return artistaService.atualizar(usuarioLogado.id(), req);
    }

    @GetMapping("/{id}")
    public ArtistaResponse buscar(@PathVariable UUID id) {
        return artistaService.buscarPorId(id);
    }
}
