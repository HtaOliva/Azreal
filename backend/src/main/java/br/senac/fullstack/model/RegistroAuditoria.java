package br.senac.fullstack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/**
 * Trilha imutavel de auditoria das mudancas de status, acoes de proposta,
 * termo de piloto e registros oficiais de POC (RNF08).
 */
@Entity
@Table(name = "tb_registro_auditoria")
@Getter
@Setter
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proposta_poc_id", nullable = false, updatable = false)
    private UUID propostaPocId;

    @Column(name = "termo_piloto_id", updatable = false)
    private UUID termoPilotoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusProposta statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", length = 30)
    private StatusProposta statusNovo;

    @Column(name = "usuario_responsavel", length = 180, nullable = false, updatable = false)
    private String usuarioResponsavel;

    @Column(name = "acao", length = 60, nullable = false, updatable = false)
    private String acao;

    @Column(name = "detalhes", columnDefinition = "text", updatable = false)
    private String detalhes;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    public RegistroAuditoria() {
        this.id = UUID.randomUUID();
        this.dataHora = LocalDateTime.now(ZoneId.systemDefault());
    }

    public RegistroAuditoria(
            UUID propostaPocId,
            UUID termoPilotoId,
            StatusProposta statusAnterior,
            StatusProposta statusNovo,
            String usuarioResponsavel,
            String acao,
            String detalhes) {
        this();
        this.propostaPocId = propostaPocId;
        this.termoPilotoId = termoPilotoId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.usuarioResponsavel = usuarioResponsavel != null ? usuarioResponsavel : "SISTEMA";
        this.acao = acao != null ? acao : "TRANSICAO_STATUS";
        this.detalhes = detalhes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegistroAuditoria that = (RegistroAuditoria) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
