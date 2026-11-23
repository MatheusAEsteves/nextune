package com.nextune.artista.controller;

import com.nextune.artista.dto.ArtistaRequest;
import com.nextune.artista.dto.ArtistaResponse;
import com.nextune.artista.service.ArtistaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/artistas")
public class ArtistaController {

    private final ArtistaService artistaService;

    public ArtistaController(ArtistaService artistaService) {
        this.artistaService = artistaService;
    }

    // PROVISÓRIO: usuarioId virá do token
    @PostMapping("/ativar")
    @ResponseStatus(HttpStatus.CREATED)
    public ArtistaResponse ativar(@RequestParam UUID usuarioId,
                                  @Valid @RequestBody ArtistaRequest req) {
        return artistaService.ativar(usuarioId, req);
    }

    // PROVISÓRIO: usuarioId virá do token. Devolve 404 se o usuário não é artista.
    @GetMapping("/me")
    public ArtistaResponse meuPerfil(@RequestParam UUID usuarioId) {
        return artistaService.buscarPorUsuario(usuarioId);
    }

    @PutMapping("/me")
    public ArtistaResponse atualizar(@RequestParam UUID usuarioId,
                                     @Valid @RequestBody ArtistaRequest req) {
        return artistaService.atualizar(usuarioId, req);
    }

    @GetMapping("/{id}")
    public ArtistaResponse buscar(@PathVariable UUID id) {
        return artistaService.buscarPorId(id);
    }
}
