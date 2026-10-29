package br.senac.fullstack.model;

import lombok.Getter;
import lombok.Setter;

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
@Getter
@Setter
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

    public void setTipoApoio(String tipoApoio) {
        this.tipoApoio = tipoApoio;
    }
}
