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
@Table(name = "tb_tema")
@Getter
@Setter
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", length = 120, nullable = false, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoVocabulario tipo = TipoVocabulario.TEMA;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public Tema() {
        this.id = UUID.randomUUID();
        this.dataCadastro = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
