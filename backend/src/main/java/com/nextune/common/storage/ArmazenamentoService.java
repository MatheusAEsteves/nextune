package com.nextune.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Guarda os uploads (áudio, capa, foto de perfil) em disco e devolve o caminho público
 * (ex.: /uploads/audio/UUID.mp3), que é o valor gravado no banco e servido pelo WebConfig.
 * O nome do arquivo é sempre gerado pelo servidor; o nome enviado pelo cliente só fornece a extensão.
 */
@Service
public class ArmazenamentoService {

    public static final String PREFIXO_URL = "/uploads/";

    public static final Set<String> EXT_AUDIO = Set.of("mp3", "wav", "ogg", "m4a", "aac", "flac");
    // SVG fica de fora de propósito: pode carregar script
    public static final Set<String> EXT_IMAGEM = Set.of("jpg", "jpeg", "png", "webp");

    public static final long LIMITE_AUDIO_BYTES = 20L * 1024 * 1024;
    public static final long LIMITE_IMAGEM_BYTES = 5L * 1024 * 1024;

    private final Path raiz;

    public ArmazenamentoService(@Value("${nextune.storage.dir:./uploads}") String diretorio) {
        this.raiz = Paths.get(diretorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(raiz);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar a pasta de uploads: " + raiz, e);
        }
    }

    public Path getRaiz() {
        return raiz;
    }

    /** Valida e grava o arquivo. Retorna o caminho público, ex.: /uploads/capa/UUID.png */
    public String salvar(MultipartFile arquivo, String pasta, Set<String> extensoesPermitidas, long limiteBytes) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo obrigatório");
        }
        if (arquivo.getSize() > limiteBytes) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(413),
                    "Arquivo maior que o limite de " + (limiteBytes / (1024 * 1024)) + " MB");
        }
        String extensao = extensao(arquivo.getOriginalFilename());
        if (extensao == null || !extensoesPermitidas.contains(extensao)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato não suportado. Aceitos: " + String.join(", ", new TreeSet<>(extensoesPermitidas)));
        }

        String nome = UUID.randomUUID() + "." + extensao;
        Path pastaDestino = raiz.resolve(pasta).normalize();
        Path destino = pastaDestino.resolve(nome).normalize();
        if (!destino.startsWith(raiz)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caminho de arquivo inválido");
        }

        try {
            Files.createDirectories(pastaDestino);
            try (InputStream entrada = arquivo.getInputStream()) {
                Files.copy(entrada, destino);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao salvar o arquivo", e);
        }
        return PREFIXO_URL + pasta + "/" + nome;
    }

    /** Apaga um arquivo previamente salvo por este serviço. Ignora null e URLs externas. */
    public void remover(String caminhoPublico) {
        if (caminhoPublico == null || !caminhoPublico.startsWith(PREFIXO_URL)) {
            return;
        }
        Path alvo = raiz.resolve(caminhoPublico.substring(PREFIXO_URL.length())).normalize();
        if (!alvo.startsWith(raiz)) {
            return;
        }
        try {
            Files.deleteIfExists(alvo);
        } catch (IOException ignorada) {
            // arquivo órfão não deve derrubar a requisição
        }
    }

    private static String extensao(String nomeOriginal) {
        if (nomeOriginal == null) {
            return null;
        }
        String ext = StringUtils.getFilenameExtension(StringUtils.cleanPath(nomeOriginal));
        return ext == null ? null : ext.toLowerCase(Locale.ROOT);
    }
}
