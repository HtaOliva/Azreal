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
import java.util.UUID;

@Entity
@Table(name = "tb_proposta_poc")
@Getter
@Setter
public class PropostaPoc {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "startup_id", nullable = false)
    private UUID startupId;

    @Column(name = "desafio_validacao_id", nullable = false)
    private UUID desafioValidacaoId;

    @Column(name = "escopo_teste", columnDefinition = "text", nullable = false)
    private String escopoTeste;

    @Column(name = "duracao_sugerida_dias", nullable = false)
    private Integer duracaoSugeridaDias;

    @Column(name = "responsabilidades_startup", columnDefinition = "text")
    private String responsabilidadesStartup;

    @Column(name = "responsabilidades_ambiente", columnDefinition = "text")
    private String responsabilidadesAmbiente;

    @Column(name = "metrica_validacao", columnDefinition = "text", nullable = false)
    private String metricaValidacao;

    @Column(name = "aceite_previo_termo_sigilo", nullable = false)
    private boolean aceitePrevioTermoSigilo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private StatusProposta status = StatusProposta.PENDENTE;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public PropostaPoc() {
        this.id = UUID.randomUUID();
        this.dataCriacao = LocalDateTime.now(ZoneId.systemDefault());
        this.status = StatusProposta.PENDENTE;
    }

    public PropostaPoc(UUID startupId, UUID desafioValidacaoId, String escopoTeste,
                       Integer duracaoSugeridaDias, String responsabilidadesStartup,
                       String responsabilidadesAmbiente, String metricaValidacao,
                       boolean aceitePrevioTermoSigilo) {
        this();
        this.startupId = startupId;
        this.desafioValidacaoId = desafioValidacaoId;
        this.escopoTeste = escopoTeste;
        this.duracaoSugeridaDias = duracaoSugeridaDias;
        this.responsabilidadesStartup = responsabilidadesStartup;
        this.responsabilidadesAmbiente = responsabilidadesAmbiente;
        this.metricaValidacao = metricaValidacao;
        this.aceitePrevioTermoSigilo = aceitePrevioTermoSigilo;
    }
}
