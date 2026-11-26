package com.nextune.common.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Cria Pageable com limites seguros (página >= 0, tamanho entre 1 e 50). */
public final class Paginacao {

    public static final int TAMANHO_MAXIMO = 50;

    private Paginacao() {}

    public static Pageable de(int pagina, int tamanho) {
        return PageRequest.of(Math.max(pagina, 0), limitar(tamanho));
    }

    public static Pageable de(int pagina, int tamanho, Sort ordenacao) {
        return PageRequest.of(Math.max(pagina, 0), limitar(tamanho), ordenacao);
    }

    private static int limitar(int tamanho) {
        return Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
    }
}
