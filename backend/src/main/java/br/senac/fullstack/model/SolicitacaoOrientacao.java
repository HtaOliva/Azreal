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
@Table(name = "tb_solicitacao_orientacao")
@Getter
@Setter
public class SolicitacaoOrientacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "startup_id", nullable = false)
    private UUID startupId;

    @Column(name = "mentor_id", nullable = false)
    private UUID mentorId;

    @Column(name = "assunto", length = 200, nullable = false)
    private String assunto;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;

    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_expiracao")
    private LocalDateTime dataExpiracao;

    public SolicitacaoOrientacao() {
        this.id = UUID.randomUUID();
        this.dataSolicitacao = LocalDateTime.now(java.time.ZoneId.systemDefault());
        this.status = StatusSolicitacao.PENDENTE;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }
}
