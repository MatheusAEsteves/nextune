package com.nextune.comentario.service;

import com.nextune.comentario.dto.ComentarioRequest;
import com.nextune.comentario.dto.ComentarioResponse;
import com.nextune.comentario.entity.ComentarioModel;
import com.nextune.comentario.mapper.ComentarioMapper;
import com.nextune.comentario.repository.ComentarioRepository;
import com.nextune.common.security.PermissaoService;
import com.nextune.common.validation.Validacoes;
import com.nextune.common.web.PaginaResponse;
import com.nextune.common.web.Paginacao;
import com.nextune.post.entity.PostModel;
import com.nextune.post.repository.PostRepository;
import com.nextune.usuario.entity.UsuarioModel;
import com.nextune.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PostRepository postRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermissaoService permissaoService;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             PostRepository postRepository,
                             UsuarioRepository usuarioRepository,
                             PermissaoService permissaoService) {
        this.comentarioRepository = comentarioRepository;
        this.postRepository = postRepository;
        this.usuarioRepository = usuarioRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional
    public ComentarioResponse criar(UUID usuarioId, UUID postId, ComentarioRequest req) {
        PostModel post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post não encontrado"));
        UsuarioModel usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String texto = Validacoes.textoObrigatorio(req.conteudo(), "conteudo", 1000);
        ComentarioModel comentario = comentarioRepository.save(new ComentarioModel(usuario, post, texto));
        return ComentarioMapper.toResponse(comentario);
    }

    /** Comentários do post, mais recentes primeiro. */
    @Transactional(readOnly = true)
    public PaginaResponse<ComentarioResponse> listar(UUID postId, int pagina, int tamanho) {
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post não encontrado");
        }
        return PaginaResponse.de(
                comentarioRepository.findByPostIdOrderByDataComentarioDesc(postId, Paginacao.de(pagina, tamanho))
                        .map(ComentarioMapper::toResponse));
    }

    /** O autor do comentário ou um administrador podem remover. */
    @Transactional
    public void remover(UUID usuarioId, UUID comentarioId) {
        ComentarioModel comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));

        boolean autor = comentario.getUsuario().getId().equals(usuarioId);
        if (!autor && !permissaoService.ehAdmin(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode remover este comentário");
        }
        comentarioRepository.delete(comentario);
    }
}
