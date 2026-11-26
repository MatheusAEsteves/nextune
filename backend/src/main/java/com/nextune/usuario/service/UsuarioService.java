package com.nextune.usuario.service;

import com.nextune.common.storage.ArmazenamentoService;
import com.nextune.common.validation.Validacoes;
import com.nextune.usuario.dto.UsuarioCreateRequest;
import com.nextune.usuario.dto.UsuarioPublicoResponse;
import com.nextune.usuario.dto.UsuarioResponse;
import com.nextune.usuario.dto.UsuarioUpdateRequest;
import com.nextune.usuario.entity.UsuarioModel;
import com.nextune.usuario.mapper.UsuarioMapper;
import com.nextune.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ArmazenamentoService armazenamento;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          ArmazenamentoService armazenamento) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.armazenamento = armazenamento;
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCreateRequest req) {
        String email = req.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        UsuarioModel usuario = new UsuarioModel(
                req.nome().trim(), email, passwordEncoder.encode(req.senha()));

        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    /** Dados completos (inclui e-mail): só para o próprio usuário. */
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id) {
        return UsuarioMapper.toResponse(buscarEntidade(id));
    }

    /** Perfil público: sem e-mail. */
    @Transactional(readOnly = true)
    public UsuarioPublicoResponse buscarPublico(UUID id) {
        return UsuarioMapper.toPublico(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponse atualizar(UUID id, UsuarioUpdateRequest req) {
        UsuarioModel usuario = buscarEntidade(id);

        if (req.nome() != null) {
            usuario.setNome(Validacoes.textoObrigatorio(req.nome(), "nome", 150));
        }
        if (req.bio() != null) {
            usuario.setBio(Validacoes.textoOpcional(req.bio(), "bio", 2000));
        }
        // Se o front devolver o valor que já está salvo (ex.: /uploads/perfil/x.jpg), não é alteração.
        if (req.fotoPerfil() != null && !req.fotoPerfil().equals(usuario.getFotoPerfil())) {
            String nova = Validacoes.urlHttpOpcional(req.fotoPerfil(), "fotoPerfil");
            String antiga = usuario.getFotoPerfil();
            usuario.setFotoPerfil(nova);
            armazenamento.remover(antiga);
        }
        return UsuarioMapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse atualizarFoto(UUID id, MultipartFile arquivo) {
        UsuarioModel usuario = buscarEntidade(id);
        String nova = armazenamento.salvar(arquivo, "perfil",
                ArmazenamentoService.EXT_IMAGEM, ArmazenamentoService.LIMITE_IMAGEM_BYTES);
        String antiga = usuario.getFotoPerfil();
        usuario.setFotoPerfil(nova);
        armazenamento.remover(antiga);
        return UsuarioMapper.toResponse(usuario);
    }

    private UsuarioModel buscarEntidade(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}
