package br.senac.fullstack.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Column(name = "uf_ibge", length = 10)
    private String ufIbge;

    @Column(name = "municipio_ibge", length = 120)
    private String municipioIbge;

    @Column(name = "o_que_precisa_validar", columnDefinition = "text")
    private String oQuePrecisaValidar;

    @Column(name = "ligada_incubadora", nullable = false)
    private boolean ligadaIncubadora = false;

    @Column(name = "incubadora_ligada", length = 180)
    private String incubadoraLigada;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_startup_integrante", joinColumns = @JoinColumn(name = "startup_id"))
    @Column(name = "integrante", length = 120)
    private List<String> integrantes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_startup_tema", joinColumns = @JoinColumn(name = "startup_id"))
    @Column(name = "tema_id")
    private List<UUID> temasIds = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_startup_modalidade", joinColumns = @JoinColumn(name = "startup_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade", length = 30)
    private List<ModalidadeTeste> modalidadesSuportadas = new ArrayList<>();

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public Startup() {
        this.id = UUID.randomUUID();
        this.dataCadastro = LocalDateTime.now(ZoneId.systemDefault());
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
