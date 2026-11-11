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
import java.time.Year;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro Oficial de POC Verificavel (RF20).
 * Registro imutavel emitido apos confirmacao de execucao do piloto (RF19),
 * atestando escopo testado, duracao, metrica avaliada, parecer do parceiro
 * e identificador verificavel para editais, programas e clientes.
 */
@Entity
@Table(name = "tb_registro_oficial_poc")
@Getter
@Setter
public class RegistroOficialPoc {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "codigo_verificador", nullable = false, unique = true, updatable = false, length = 40)
    private String codigoVerificador;

    @Column(name = "termo_piloto_id", nullable = false, updatable = false)
    private UUID termoPilotoId;

    @Column(name = "proposta_poc_id", nullable = false, updatable = false)
    private UUID propostaPocId;

    @Column(name = "startup_id", nullable = false, updatable = false)
    private UUID startupId;

    @Column(name = "ambiente_validacao_id", nullable = false, updatable = false)
    private UUID ambienteValidacaoId;

    @Column(name = "desafio_validacao_id", updatable = false)
    private UUID desafioValidacaoId;

    @Column(name = "escopo_testado", columnDefinition = "text", nullable = false, updatable = false)
    private String escopoTestado;

    @Column(name = "duracao_dias", nullable = false, updatable = false)
    private Integer duracaoDias;

    @Enumerated(EnumType.STRING)
    @Column(name = "avaliacao_metrica", length = 30, nullable = false, updatable = false)
    private AvaliacaoMetrica avaliacaoMetrica;

    @Column(name = "parecer", columnDefinition = "text", nullable = false, updatable = false)
    private String parecer;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_privacidade", length = 30, nullable = false, updatable = false)
    private ModoPrivacidadePoc modoPrivacidade;

    @Column(name = "direcionamento_pos_poc", length = 120, updatable = false)
    private String direcionamentoPosPoc;

    @Column(name = "metrica_validacao", columnDefinition = "text", updatable = false)
    private String metricaValidacao;

    @Column(name = "resumo_entrega", columnDefinition = "text", updatable = false)
    private String resumoEntrega;

    @Column(name = "nome_organizacao", length = 180, updatable = false)
    private String nomeOrganizacao;

    @Column(name = "setor_organizacao", length = 120, updatable = false)
    private String setorOrganizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "porte_organizacao", length = 40, updatable = false)
    private PorteOrganizacao porteOrganizacao;

    @Column(name = "data_emissao", nullable = false, updatable = false)
    private LocalDateTime dataEmissao;

    public RegistroOficialPoc() {
        this.id = UUID.randomUUID();
        this.codigoVerificador = gerarCodigoVerificador();
        this.dataEmissao = LocalDateTime.now(ZoneId.systemDefault());
    }

    public static String gerarCodigoVerificador() {
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "POC-" + Year.now().getValue() + "-" + randomPart;
    }

    public static RegistroOficialPoc emitir(
            TermoPiloto termo,
            String nomeOrganizacao,
            String setorOrganizacao,
            PorteOrganizacao porteOrganizacao) {
        if (termo == null) {
            throw new IllegalArgumentException("Termo de piloto nao pode ser nulo para emissao de registro");
        }
        if (termo.getStatus() != StatusProposta.POC_CONCLUIDA) {
            throw new IllegalStateException("Apenas pilotos concluidos podem emitir Registro Oficial de POC");
        }
        if (termo.getAvaliacaoMetrica() == null) {
            throw new IllegalStateException("Avaliacao da metrica e obrigatoria para emissao do registro");
        }
        if (termo.getParecer() == null || termo.getParecer().isBlank()) {
            throw new IllegalStateException("Parecer do ambiente e obrigatorio para emissao do registro");
        }

        RegistroOficialPoc registro = new RegistroOficialPoc();
        registro.setTermoPilotoId(termo.getId());
        registro.setPropostaPocId(termo.getPropostaPocId());
        registro.setStartupId(termo.getStartupId());
        registro.setAmbienteValidacaoId(termo.getAmbienteValidacaoId());
        registro.setDesafioValidacaoId(termo.getDesafioValidacaoId());
        registro.setEscopoTestado(termo.getEscopoTeste());
        registro.setDuracaoDias(termo.getDuracaoDias());
        registro.setAvaliacaoMetrica(termo.getAvaliacaoMetrica());
        registro.setParecer(termo.getParecer());
        registro.setModoPrivacidade(termo.getModoPrivacidade() != null
                ? termo.getModoPrivacidade() : ModoPrivacidadePoc.EXIBICAO_NOMINAL);
        registro.setDirecionamentoPosPoc(termo.getDirecionamentoPosPoc());
        registro.setMetricaValidacao(termo.getMetricaValidacao());
        registro.setResumoEntrega(termo.getResumoEntrega());
        registro.setSetorOrganizacao(setorOrganizacao);
        registro.setPorteOrganizacao(porteOrganizacao);

        if (registro.getModoPrivacidade() == ModoPrivacidadePoc.EXIBICAO_NOMINAL) {
            registro.setNomeOrganizacao(nomeOrganizacao);
        } else {
            registro.setNomeOrganizacao(null);
        }

        return registro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegistroOficialPoc that = (RegistroOficialPoc) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
