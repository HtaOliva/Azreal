package br.senac.fullstack.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_startup")
@Getter
@Setter
public class Startup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", length = 120, nullable = false)
    private String nome;

    @Column(name = "email", length = 180, nullable = false, unique = true)
    private String email;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "area_atuacao", length = 120)
    private String areaAtuacao;

    @Column(name = "data_fundacao")
    private LocalDate dataFundacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "estagio_atual", length = 30)
    private Estagio estagioAtual;

    @Column(name = "localidade_sede", length = 120)
    private String localidadeSede;

    @Column(name = "localidade_atuacao", length = 120)
    private String localidadeAtuacao;

    @Column(name = "ligada_incubadora", nullable = false)
    private boolean ligadaIncubadora = false;

    @Column(name = "incubadora_ligada", length = 180)
    private String incubadoraLigada;

    @jakarta.persistence.ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
    @jakarta.persistence.CollectionTable(name = "tb_startup_integrante", joinColumns = @jakarta.persistence.JoinColumn(name = "startup_id"))
    @jakarta.persistence.Column(name = "integrante", length = 120)
    private List<String> integrantes = new ArrayList<>();

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public Startup() {
        this.id = UUID.randomUUID();
        this.dataCadastro = LocalDateTime.now(java.time.ZoneId.systemDefault());
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    public void setLocalidadeSede(String localidadeSede) {
        this.localidadeSede = localidadeSede;
    }
    public void setIntegrantes(List<String> integrantes) {
        this.integrantes = integrantes;
    }
}