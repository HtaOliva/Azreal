package br.senac.fullstack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Modelo de Termo de Piloto congelado (RF15).
 * Captura um snapshot imutável do escopo acordado, duração, responsabilidades,
 * métrica de validação e data/hora do aceite.
 */
@Entity
@Table(name = "tb_termo_piloto")
@Getter
@Setter
public class TermoPiloto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proposta_poc_id", nullable = false, updatable = false)
    private UUID propostaPocId;

    @Column(name = "startup_id", nullable = false, updatable = false)
    private UUID startupId;

    @Column(name = "desafio_validacao_id", nullable = false, updatable = false)
    private UUID desafioValidacaoId;

    @Column(name = "ambiente_validacao_id", nullable = false, updatable = false)
    private UUID ambienteValidacaoId;

    @Column(name = "escopo_teste", columnDefinition = "text", nullable = false, updatable = false)
    private String escopoTeste;

    @Column(name = "duracao_dias", nullable = false, updatable = false)
    private Integer duracaoDias;

    @Column(name = "responsabilidades_startup", columnDefinition = "text", updatable = false)
    private String responsabilidadesStartup;

    @Column(name = "responsabilidades_ambiente", columnDefinition = "text", updatable = false)
    private String responsabilidadesAmbiente;

    @Column(name = "metrica_validacao", columnDefinition = "text", nullable = false, updatable = false)
    private String metricaValidacao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public TermoPiloto() {
        this.id = UUID.randomUUID();
        this.dataCriacao = LocalDateTime.now(ZoneId.systemDefault());
    }

    public static TermoPiloto congelar(PropostaPoc proposta, UUID ambienteValidacaoId) {
        if (proposta == null) {
            throw new IllegalArgumentException("Proposta de POC nao pode ser nula");
        }
        TermoPiloto termo = new TermoPiloto();
        termo.setPropostaPocId(proposta.getId());
        termo.setStartupId(proposta.getStartupId());
        termo.setDesafioValidacaoId(proposta.getDesafioValidacaoId());
        termo.setAmbienteValidacaoId(ambienteValidacaoId);
        termo.setEscopoTeste(proposta.getEscopoTeste());
        termo.setDuracaoDias(proposta.getDuracaoSugeridaDias());
        termo.setResponsabilidadesStartup(proposta.getResponsabilidadesStartup());
        termo.setResponsabilidadesAmbiente(proposta.getResponsabilidadesAmbiente());
        termo.setMetricaValidacao(proposta.getMetricaValidacao());
        return termo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TermoPiloto that = (TermoPiloto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
