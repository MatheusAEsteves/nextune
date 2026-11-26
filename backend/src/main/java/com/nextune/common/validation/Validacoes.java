package com.nextune.common.validation;

import java.util.regex.Pattern;

/** Validações reutilizáveis para texto e URLs. Texto em branco vira null nos campos opcionais. */
public final class Validacoes {

    private static final Pattern URL_HTTP = Pattern.compile("^https?://\\S+$", Pattern.CASE_INSENSITIVE);

    private Validacoes() {}

    public static String textoObrigatorio(String valor, String campo, int max) {
        String v = valor == null ? "" : valor.trim();
        if (v.isEmpty()) {
            throw new ValidacaoException(campo, "Campo obrigatório");
        }
        if (v.length() > max) {
            throw new ValidacaoException(campo, "Máximo de " + max + " caracteres");
        }
        return v;
    }

    /** Retorna null quando o valor é null ou está em branco. */
    public static String textoOpcional(String valor, String campo, int max) {
        if (valor == null) {
            return null;
        }
        String v = valor.trim();
        if (v.isEmpty()) {
            return null;
        }
        if (v.length() > max) {
            throw new ValidacaoException(campo, "Máximo de " + max + " caracteres");
        }
        return v;
    }

    public static String urlHttpObrigatoria(String valor, String campo) {
        String v = textoObrigatorio(valor, campo, 255);
        exigirHttp(v, campo);
        return v;
    }

    /** Retorna null quando o valor é null ou está em branco. */
    public static String urlHttpOpcional(String valor, String campo) {
        String v = textoOpcional(valor, campo, 255);
        if (v != null) {
            exigirHttp(v, campo);
        }
        return v;
    }

    private static void exigirHttp(String v, String campo) {
        if (!URL_HTTP.matcher(v).matches()) {
            throw new ValidacaoException(campo, "Informe uma URL completa começando com http:// ou https://");
        }
    }
}
