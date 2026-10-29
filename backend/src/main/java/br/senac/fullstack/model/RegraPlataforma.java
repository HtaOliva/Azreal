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
@Table(name = "tb_regra_plataforma")
@Getter
@Setter
public class RegraPlataforma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "texto", columnDefinition = "text", nullable = false)
    private String texto;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    public RegraPlataforma() {
        this.id = UUID.randomUUID();
        this.dataAtualizacao = LocalDateTime.now(java.time.ZoneId.systemDefault());
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}