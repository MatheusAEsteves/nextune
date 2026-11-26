package com.nextune.usuario.controller;

import com.nextune.common.security.UsuarioLogado;
import com.nextune.usuario.dto.UsuarioCreateRequest;
import com.nextune.usuario.dto.UsuarioPublicoResponse;
import com.nextune.usuario.dto.UsuarioResponse;
import com.nextune.usuario.dto.UsuarioUpdateRequest;
import com.nextune.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioLogado usuarioLogado;

    public UsuarioController(UsuarioService usuarioService, UsuarioLogado usuarioLogado) {
        this.usuarioService = usuarioService;
        this.usuarioLogado = usuarioLogado;
    }

    /** Cadastro (rota pública). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioCreateRequest req) {
        return usuarioService.criar(req);
    }

    /** Perfil completo do usuário logado (inclui e-mail). */
    @GetMapping("/me")
    public UsuarioResponse meuPerfil() {
        return usuarioService.buscarPorId(usuarioLogado.id());
    }

    @PutMapping("/me")
    public UsuarioResponse atualizar(@Valid @RequestBody UsuarioUpdateRequest req) {
        return usuarioService.atualizar(usuarioLogado.id(), req);
    }

    /** Upload da foto de perfil (multipart, campo "foto"). */
    @PostMapping(value = "/me/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioResponse atualizarFoto(@RequestParam("foto") MultipartFile foto) {
        return usuarioService.atualizarFoto(usuarioLogado.id(), foto);
    }

    /** Perfil público de outro usuário (sem e-mail). */
    @GetMapping("/{id}")
    public UsuarioPublicoResponse buscar(@PathVariable UUID id) {
        return usuarioService.buscarPublico(id);
    }
}
