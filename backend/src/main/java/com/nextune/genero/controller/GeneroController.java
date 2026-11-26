package com.nextune.genero.controller;

import com.nextune.genero.dto.GeneroResponse;
import com.nextune.genero.service.GeneroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/generos")
public class GeneroController {

    private final GeneroService generoService;

    public GeneroController(GeneroService generoService) {
        this.generoService = generoService;
    }

    /** Lista todos os gêneros em ordem alfabética (usado no onboarding e no cadastro de posts). */
    @GetMapping
    public List<GeneroResponse> listar() {
        return generoService.listar();
    }
}
