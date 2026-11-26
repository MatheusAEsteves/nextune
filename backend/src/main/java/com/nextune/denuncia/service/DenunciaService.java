package com.nextune.denuncia.service;

import com.nextune.artista.entity.ArtistaModel;
import com.nextune.artista.repository.ArtistaRepository;
import com.nextune.common.security.PermissaoService;
import com.nextune.common.validation.ValidacaoException;
import com.nextune.common.validation.Validacoes;
import com.nextune.common.web.PaginaResponse;
import com.nextune.common.web.Paginacao;
import com.nextune.denuncia.dto.DenunciaRequest;
import com.nextune.denuncia.dto.DenunciaResponse;
import com.nextune.denuncia.entity.DenunciaModel;
import com.nextune.denuncia.entity.DenunciaStatus;
import com.nextune.denuncia.repository.DenunciaRepository;
import com.nextune.post.entity.PostModel;
import com.nextune.post.repository.PostRepository;
import com.nextune.usuario.entity.UsuarioModel;
import com.nextune.usuario.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final PostRepository postRepository;
    private final ArtistaRepository artistaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermissaoService permissaoService;

    public DenunciaService(DenunciaRepository denunciaRepository,
                           PostRepository postRepository,
                           ArtistaRepository artistaRepository,
                           UsuarioRepository usuarioRepository,
                           PermissaoService permissaoService) {
        this.denunciaRepository = denunciaRepository;
        this.postRepository = postRepository;
        this.artistaRepository = artistaRepository;
        this.usuarioRepository = usuarioRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional
    public DenunciaResponse criar(UUID usuarioId, DenunciaRequest req) {
        if ((req.postId() == null) == (req.artistaId() == null)) {
            throw new ValidacaoException("postId", "Informe o post OU o artista a ser denunciado");
        }
        String motivo = Validacoes.textoObrigatorio(req.motivo(), "motivo", 100);
        String descricao = Validacoes.textoOpcional(req.descricao(), "descricao", 2000);

        UsuarioModel usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        PostModel post = null;
        ArtistaModel artista = null;
        if (req.postId() != null) {
            post = postRepository.findById(req.postId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post não encontrado"));
            if (denunciaRepository.existsByUsuarioIdAndPostIdAndStatus(usuarioId, post.getId(), DenunciaStatus.PENDENTE)) {
                throw duplicada();
            }
        } else {
            artista = artistaRepository.findById(req.artistaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));
            if (denunciaRepository.existsByUsuarioIdAndArtistaIdAndStatus(usuarioId, artista.getId(), DenunciaStatus.PENDENTE)) {
                throw duplicada();
            }
        }

        return paraResponse(denunciaRepository.save(new DenunciaModel(usuario, post, artista, motivo, descricao)));
    }

    /** Somente administradores. Sem status, lista todas (mais recentes primeiro). */
    @Transactional(readOnly = true)
    public PaginaResponse<DenunciaResponse> listar(UUID adminId, DenunciaStatus status, int pagina, int tamanho) {
        permissaoService.exigirAdmin(adminId);
        var pageable = Paginacao.de(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataDenuncia"));
        Page<DenunciaModel> resultado = status == null
                ? denunciaRepository.findAll(pageable)
                : denunciaRepository.findByStatus(status, pageable);
        return PaginaResponse.de(resultado.map(this::paraResponse));
    }

    /** Somente administradores. */
    @Transactional
    public DenunciaResponse atualizarStatus(UUID adminId, UUID denunciaId, DenunciaStatus novoStatus) {
        permissaoService.exigirAdmin(adminId);
        DenunciaModel denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Denúncia não encontrada"));
        denuncia.setStatus(novoStatus);
        return paraResponse(denuncia);
    }

    private DenunciaResponse paraResponse(DenunciaModel d) {
        return new DenunciaResponse(
                d.getId(),
                d.getUsuario().getId(),
                d.getPost() != null ? d.getPost().getId() : null,
                d.getArtista() != null ? d.getArtista().getId() : null,
                d.getMotivo(), d.getDescricao(), d.getStatus(), d.getDataDenuncia());
    }

    private static ResponseStatusException duplicada() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Você já tem uma denúncia pendente para este alvo");
    }
}
