package com.nextune.feed.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interacao")
public class InteracaoModel {

    // Peso de cada ação na pontuação de gênero do usuário
    public static final int PONTOS_CURTIDA = 3;
    public static final int PONTOS_SALVO = 2;
    public static final int PONTOS_PULO = -1;

    @EmbeddedId
    private InteracaoId id;

    @Column(nullable = false)
    private boolean curtido = false;

    @Column(nullable = false)
    private boolean salvo = false;

    @Column(nullable = false)
    private boolean pulou = false;

    @Column(name = "segundos_ouvidos", nullable = false)
    private int segundosOuvidos = 0;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    protected InteracaoModel() {}

    public InteracaoModel(UUID usuarioId, UUID postId) {
        this.id = new InteracaoId(usuarioId, postId);
    }

    // As colunas de data são NOT NULL e sem default no banco: preenchidas aqui, não pelo MySQL.
    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        this.dataCriacao = agora;
        this.dataAtualizacao = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    /** Pontos que esta interação vale para os gêneros do post, no estado atual. */
    public int pontos() {
        return (curtido ? PONTOS_CURTIDA : 0)
                + (salvo ? PONTOS_SALVO : 0)
                + (pulou ? PONTOS_PULO : 0);
    }

    public InteracaoId getId() { return id; }
    public boolean isCurtido() { return curtido; }
    public boolean isSalvo() { return salvo; }
    public boolean isPulou() { return pulou; }
    public int getSegundosOuvidos() { return segundosOuvidos; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }

    public void setCurtido(boolean curtido) { this.curtido = curtido; }
    public void setSalvo(boolean salvo) { this.salvo = salvo; }
    public void setPulou(boolean pulou) { this.pulou = pulou; }
    public void setSegundosOuvidos(int segundosOuvidos) { this.segundosOuvidos = segundosOuvidos; }
}
