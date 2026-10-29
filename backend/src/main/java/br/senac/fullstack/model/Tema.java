package br.senac.fullstack.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
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

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public Tema() {
        this.id = UUID.randomUUID();
        this.dataCadastro = LocalDateTime.now(java.time.ZoneId.systemDefault());
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}