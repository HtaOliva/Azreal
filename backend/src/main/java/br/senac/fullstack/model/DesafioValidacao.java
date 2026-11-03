package br.senac.fullstack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "tb_desafio_validacao")
@Getter
@Setter
public class DesafioValidacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ambiente_validacao_id", nullable = false)
    private AmbienteValidacao ambienteValidacao;

    @Column(name = "titulo", length = 180, nullable = false)
    private String titulo;

    @Column(name = "descricao_problema", columnDefinition = "text", nullable = false)
    private String descricaoProblema;

    @Column(name = "publico_alvo", length = 255)
    private String publicoAlvo;

    @Column(name = "tamanho_amostra")
    private Integer tamanhoAmostra;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade", length = 30)
    private ModalidadeTeste modalidade;

    @Column(name = "duracao_maxima")
    private Integer duracaoMaxima;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private StatusDesafio status = StatusDesafio.ABERTO;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public DesafioValidacao() {
        this.id = UUID.randomUUID();
        this.dataCriacao = LocalDateTime.now(ZoneId.systemDefault());
    }

    public String getUfIbge() {
        return ambienteValidacao != null ? ambienteValidacao.getUfIbge() : null;
    }

    public String getMunicipioIbge() {
        return ambienteValidacao != null ? ambienteValidacao.getMunicipioIbge() : null;
    }
}
