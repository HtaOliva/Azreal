package br.senac.fullstack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_recomendacao")
public class Recomendacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "mentor_id", nullable = false)
    private UUID mentorId;

    @Column(name = "startup_id", nullable = false)
    private UUID startupId;

    @Column(name = "tipo_apoio", length = 120, nullable = false)
    private String tipoApoio;

    @Column(name = "observacao", columnDefinition = "text")
    private String observacao;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    public Recomendacao() {
        this.id = UUID.randomUUID();
        this.dataHora = LocalDateTime.now(java.time.ZoneId.systemDefault());
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getMentorId() {
        return mentorId;
    }
    public void setMentorId(UUID mentorId) {
        this.mentorId = mentorId;
    }
    public UUID getStartupId() {
        return startupId;
    }
    public void setStartupId(UUID startupId) {
        this.startupId = startupId;
    }
    public String getTipoApoio() {
        return tipoApoio;
    }
    public void setTipoApoio(String tipoApoio) {
        this.tipoApoio = tipoApoio;
    }
    public String getObservacao() {
        return observacao;
    }
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
