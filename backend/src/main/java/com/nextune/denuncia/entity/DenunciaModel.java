package com.nextune.denuncia.entity;

import com.nextune.artista.entity.ArtistaModel;
import com.nextune.post.entity.PostModel;
import com.nextune.usuario.entity.UsuarioModel;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "denuncia")
public class DenunciaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private UsuarioModel usuario;

    // Uma denúncia mira um post OU um artista (o outro fica null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", updatable = false)
    private PostModel post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artista_id", updatable = false)
    private ArtistaModel artista;

    @Column(nullable = false, length = 100, updatable = false)
    private String motivo;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DenunciaStatus status = DenunciaStatus.PENDENTE;

    @Column(name = "data_denuncia", nullable = false, updatable = false)
    private LocalDateTime dataDenuncia;

    protected DenunciaModel() {}

    public DenunciaModel(UsuarioModel usuario, PostModel post, ArtistaModel artista,
                         String motivo, String descricao) {
        this.usuario = usuario;
        this.post = post;
        this.artista = artista;
        this.motivo = motivo;
        this.descricao = descricao;
        this.dataDenuncia = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UsuarioModel getUsuario() { return usuario; }
    public PostModel getPost() { return post; }
    public ArtistaModel getArtista() { return artista; }
    public String getMotivo() { return motivo; }
    public String getDescricao() { return descricao; }
    public DenunciaStatus getStatus() { return status; }
    public LocalDateTime getDataDenuncia() { return dataDenuncia; }

    public void setStatus(DenunciaStatus status) { this.status = status; }
}
