package com.nextune.usuario.controller;

import com.nextune.usuario.dto.UsuarioCreateRequest;
import com.nextune.usuario.dto.UsuarioResponse;
import com.nextune.usuario.dto.UsuarioUpdateRequest;
import com.nextune.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioCreateRequest req) {
        return usuarioService.criar(req);
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id);
    }

    // PROVISÓRIO: o id virá do token quando a segurança estiver pronta
    @PutMapping("/me")
    public UsuarioResponse atualizar(@RequestParam UUID usuarioId,
                                     @Valid @RequestBody UsuarioUpdateRequest req) {
        return usuarioService.atualizar(usuarioId, req);
    }
}
