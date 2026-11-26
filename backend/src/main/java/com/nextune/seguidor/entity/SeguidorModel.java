package com.nextune.seguidor.entity;

import com.nextune.artista.entity.ArtistaModel;
import com.nextune.usuario.entity.UsuarioModel;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "seguidor",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "artista_id"})
)
public class SeguidorModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private UsuarioModel usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artista_id", nullable = false, updatable = false)
    private ArtistaModel artista;

    @Column(name = "data_seguimento", nullable = false, updatable = false)
    private LocalDateTime dataSeguimento;

    protected SeguidorModel() {}

    public SeguidorModel(UsuarioModel usuario, ArtistaModel artista) {
        this.usuario = usuario;
        this.artista = artista;
        this.dataSeguimento = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UsuarioModel getUsuario() { return usuario; }
    public ArtistaModel getArtista() { return artista; }
    public LocalDateTime getDataSeguimento() { return dataSeguimento; }
}
