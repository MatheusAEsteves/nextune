package com.nextune.comentario.entity;

import com.nextune.post.entity.PostModel;
import com.nextune.usuario.entity.UsuarioModel;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comentario")
public class ComentarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private UsuarioModel usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private PostModel post;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "data_comentario", nullable = false, updatable = false)
    private LocalDateTime dataComentario;

    protected ComentarioModel() {}

    public ComentarioModel(UsuarioModel usuario, PostModel post, String conteudo) {
        this.usuario = usuario;
        this.post = post;
        this.conteudo = conteudo;
        this.dataComentario = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UsuarioModel getUsuario() { return usuario; }
    public PostModel getPost() { return post; }
    public String getConteudo() { return conteudo; }
    public LocalDateTime getDataComentario() { return dataComentario; }
}
