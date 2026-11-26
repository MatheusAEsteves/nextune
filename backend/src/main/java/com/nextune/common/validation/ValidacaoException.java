package com.nextune.common.validation;

import java.util.Map;

/**
 * Erro de validação de regra de negócio. Vira HTTP 400 com o mapa "campos"
 * (mesmo formato dos erros do Bean Validation), para o front mostrar a mensagem no campo certo.
 */
public class ValidacaoException extends RuntimeException {

    private final Map<String, String> campos;

    public ValidacaoException(String campo, String mensagem) {
        super(mensagem);
        this.campos = Map.of(campo, mensagem);
    }

    public ValidacaoException(Map<String, String> campos) {
        super("Dados inválidos");
        this.campos = Map.copyOf(campos);
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
