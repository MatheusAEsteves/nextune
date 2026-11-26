package com.nextune.common.web;

import org.springframework.data.domain.Page;

import java.util.List;

/** Página de resultados em formato estável para o front (em vez de serializar o PageImpl). */
public record PaginaResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas) {

    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(
                page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
