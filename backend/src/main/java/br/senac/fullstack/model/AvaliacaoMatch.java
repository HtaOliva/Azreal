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
 * Entidade para avaliacao pos-conclusao da qualidade do matchmaking (RF21).
 * Permite medir a precisao do encaixe problema-solucao entre startup e parceiro.
 */
@Entity
@Table(name = "tb_avaliacao_match")
@Getter
@Setter
public class AvaliacaoMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proposta_poc_id", nullable = false, updatable = false)
    private UUID propostaPocId;

    @Column(name = "termo_piloto_id")
    private UUID termoPilotoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_avaliador", length = 30, nullable = false)
    private TipoAvaliadorMatch tipoAvaliador;

    @Column(name = "usuario_responsavel", length = 180, nullable = false)
    private String usuarioResponsavel;

    @Column(name = "encaixe_fez_sentido", nullable = false)
    private Boolean encaixeFezSentido;

    @Column(name = "nota_qualidade", nullable = false)
    private Integer notaQualidade;

    @Column(name = "comentario", columnDefinition = "text")
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_match", length = 20, nullable = false)
    private OrigemMatch origemMatch = OrigemMatch.IA;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public AvaliacaoMatch() {
        this.id = UUID.randomUUID();
        this.dataCriacao = LocalDateTime.now(ZoneId.systemDefault());
        this.origemMatch = OrigemMatch.IA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AvaliacaoMatch that = (AvaliacaoMatch) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
