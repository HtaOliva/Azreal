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
@Table(name = "tb_anotacao_mentoria")
public class AnotacaoMentoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "mentor_id", nullable = false)
    private UUID mentorId;

    @Column(name = "startup_id", nullable = false)
    private UUID startupId;

    @Column(name = "conteudo", columnDefinition = "text", nullable = false)
    private String conteudo;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    public AnotacaoMentoria() {
        this.id = UUID.randomUUID();
        this.dataHora = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMentorId() { return mentorId; }
    public void setMentorId(UUID mentorId) { this.mentorId = mentorId; }
    public UUID getStartupId() { return startupId; }
    public void setStartupId(UUID startupId) { this.startupId = startupId; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
