package com.nextune.feed.entity;

import com.nextune.genero.entity.GeneroModel;
import com.nextune.usuario.entity.UsuarioModel;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "preferencia_usuario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "genero_id"})
)
public class PreferenciaUsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private UsuarioModel usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "genero_id", nullable = false, updatable = false)
    private GeneroModel genero;

    @Column(nullable = false)
    private int pontuacao = 0;

    // Coluna NOT NULL sem default no banco: preenchida aqui, não pelo MySQL.
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    protected PreferenciaUsuarioModel() {}

    public PreferenciaUsuarioModel(UsuarioModel usuario, GeneroModel genero, int pontuacao) {
        this.usuario = usuario;
        this.genero = genero;
        this.pontuacao = pontuacao;
    }

    @PrePersist
    @PreUpdate
    protected void marcarAtualizacao() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UsuarioModel getUsuario() { return usuario; }
    public GeneroModel getGenero() { return genero; }
    public int getPontuacao() { return pontuacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }

    public void adicionarPontos(int pontos) { this.pontuacao += pontos; }

    /** Garante que o gênero conte como "gostado" (pontuação positiva) sem diminuir quem já tem mais. */
    public void garantirPontuacaoMinima(int minimo) {
        if (this.pontuacao < minimo) {
            this.pontuacao = minimo;
        }
    }
}
