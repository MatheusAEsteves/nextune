package com.nextune.common.web;

import java.util.Map;

/** Formato único de erro da API. "campos" só vem preenchido em erros de validação. */
public record ErroResponse(
        String timestamp,
        int status,
        String erro,
        String mensagem,
        Map<String, String> campos) {}
