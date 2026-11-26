package com.nextune.denuncia.controller;

import com.nextune.common.security.UsuarioLogado;
import com.nextune.common.web.PaginaResponse;
import com.nextune.denuncia.dto.AtualizarStatusDenunciaRequest;
import com.nextune.denuncia.dto.DenunciaRequest;
import com.nextune.denuncia.dto.DenunciaResponse;
import com.nextune.denuncia.entity.DenunciaStatus;
import com.nextune.denuncia.service.DenunciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class DenunciaController {

    private final DenunciaService denunciaService;
    private final UsuarioLogado usuarioLogado;

    public DenunciaController(DenunciaService denunciaService, UsuarioLogado usuarioLogado) {
        this.denunciaService = denunciaService;
        this.usuarioLogado = usuarioLogado;
    }

    /** Denuncia um post OU um artista. */
    @PostMapping("/denuncias")
    @ResponseStatus(HttpStatus.CREATED)
    public DenunciaResponse criar(@Valid @RequestBody DenunciaRequest req) {
        return denunciaService.criar(usuarioLogado.id(), req);
    }

    /** Moderação: lista denúncias (opcionalmente filtradas por status). Somente ADMIN. */
    @GetMapping("/admin/denuncias")
    public PaginaResponse<DenunciaResponse> listar(@RequestParam(required = false) DenunciaStatus status,
                                                   @RequestParam(defaultValue = "0") int pagina,
                                                   @RequestParam(defaultValue = "20") int tamanho) {
        return denunciaService.listar(usuarioLogado.id(), status, pagina, tamanho);
    }

    /** Moderação: altera o status de uma denúncia. Somente ADMIN. */
    @PatchMapping("/admin/denuncias/{id}/status")
    public DenunciaResponse atualizarStatus(@PathVariable UUID id,
                                            @Valid @RequestBody AtualizarStatusDenunciaRequest req) {
        return denunciaService.atualizarStatus(usuarioLogado.id(), id, req.status());
    }
}
