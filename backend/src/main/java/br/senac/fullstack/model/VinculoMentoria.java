package br.senac.fullstack.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_vinculo_mentoria")
@Getter
@Setter
public class VinculoMentoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "startup_id", nullable = false)
    private UUID startupId;

    @Column(name = "mentor_id", nullable = false)
    private UUID mentorId;

    @Column(name = "data_inicio", nullable = false, updatable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusVinculo status = StatusVinculo.ATIVO;

    public VinculoMentoria() {
        this.id = UUID.randomUUID();
        this.dataInicio = LocalDateTime.now(java.time.ZoneId.systemDefault());
        this.status = StatusVinculo.ATIVO;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }
}
