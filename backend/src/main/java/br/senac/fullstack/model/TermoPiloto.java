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
 * Modelo de Termo de Piloto congelado (RF15), Entrega (RF18) e Confirmacao (RF19).
 * Captura um snapshot imutavel do escopo acordado, duracao, responsabilidades,
 * metrica de validacao, dados de entrega pela startup e confirmacao pelo parceiro.
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private StatusProposta status = StatusProposta.EM_TESTE;

    @Column(name = "resumo_entrega", columnDefinition = "text")
    private String resumoEntrega;

    @Column(name = "data_entrega")
    private LocalDateTime dataEntrega;

    @Column(name = "links_evidencias", columnDefinition = "text")
    private String linksEvidencias;

    @Column(name = "observacoes_entrega", columnDefinition = "text")
    private String observacoesEntrega;

    @Column(name = "piloto_executado")
    private Boolean pilotoExecutado;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "avaliacao_metrica", length = 30)
    private AvaliacaoMetrica avaliacaoMetrica;

    @Column(name = "parecer", columnDefinition = "text")
    private String parecer;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_privacidade", length = 30)
    private ModoPrivacidadePoc modoPrivacidade;

    @Column(name = "direcionamento_pos_poc", length = 120)
    private String direcionamentoPosPoc;

    @Column(name = "justificativa_nao_execucao", columnDefinition = "text")
    private String justificativaNaoExecucao;

    @Column(name = "certificado_emitido", nullable = false)
    private Boolean certificadoEmitido = false;

    public TermoPiloto() {
        this.id = UUID.randomUUID();
        this.dataCriacao = LocalDateTime.now(ZoneId.systemDefault());
        this.status = StatusProposta.EM_TESTE;
        this.certificadoEmitido = false;
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
        termo.setStatus(StatusProposta.EM_TESTE);
        return termo;
    }

    public void registrarEntrega(String resumoEntrega, String linksEvidencias, String observacoesEntrega) {
        if (this.status != StatusProposta.EM_TESTE) {
            throw new IllegalStateException("Apenas pilotos com status EM_TESTE podem registrar entrega");
        }
        if (resumoEntrega == null || resumoEntrega.isBlank()) {
            throw new IllegalArgumentException("O resumo do que foi entregue e obrigatorio");
        }
        this.resumoEntrega = resumoEntrega.trim();
        this.linksEvidencias = linksEvidencias != null ? linksEvidencias.trim() : null;
        this.observacoesEntrega = observacoesEntrega != null ? observacoesEntrega.trim() : null;
        this.dataEntrega = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void confirmarExecucao(
            Boolean pilotoExecutado,
            AvaliacaoMetrica avaliacaoMetrica,
            String parecer,
            ModoPrivacidadePoc modoPrivacidade,
            String direcionamentoPosPoc,
            String justificativaNaoExecucao) {
        if (this.status != StatusProposta.EM_TESTE) {
            throw new IllegalStateException("Apenas pilotos com status EM_TESTE podem ter execucao confirmada");
        }
        if (pilotoExecutado == null) {
            throw new IllegalArgumentException("A confirmacao de execucao do piloto e obrigatoria");
        }

        this.pilotoExecutado = pilotoExecutado;
        this.dataConfirmacao = LocalDateTime.now(ZoneId.systemDefault());

        if (Boolean.FALSE.equals(pilotoExecutado)) {
            this.status = StatusProposta.POC_NAO_CONFIRMADA;
            this.certificadoEmitido = false;
            this.avaliacaoMetrica = null;
            this.modoPrivacidade = null;
            this.justificativaNaoExecucao = (justificativaNaoExecucao != null && !justificativaNaoExecucao.isBlank())
                    ? justificativaNaoExecucao.trim()
                    : (parecer != null ? parecer.trim() : null);
        } else {
            if (avaliacaoMetrica == null) {
                throw new IllegalArgumentException("Avaliacao da metrica e obrigatoria quando o piloto foi executado");
            }
            if (parecer == null || parecer.isBlank()) {
                throw new IllegalArgumentException("Parecer avaliativo e obrigatorio quando o piloto foi executado");
            }
            if (modoPrivacidade == null) {
                throw new IllegalArgumentException("Modo de privacidade e obrigatorio quando o piloto foi executado");
            }
            this.status = StatusProposta.POC_CONCLUIDA;
            this.avaliacaoMetrica = avaliacaoMetrica;
            this.parecer = parecer.trim();
            this.modoPrivacidade = modoPrivacidade;
            this.direcionamentoPosPoc = direcionamentoPosPoc != null ? direcionamentoPosPoc.trim() : null;
            this.justificativaNaoExecucao = null;
            this.certificadoEmitido = false;
        }
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
