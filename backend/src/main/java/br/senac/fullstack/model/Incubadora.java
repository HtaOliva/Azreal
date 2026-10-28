package br.senac.fullstack.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_incubadora")
public class Incubadora {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", length = 180, nullable = false)
    private String nome;

    @Column(name = "foco", length = 255)
    private String foco;

    @Column(name = "criterios_entrada", columnDefinition = "text")
    private String criteriosEntrada;

    @Column(name = "localidade", length = 120)
    private String localidade;

    @ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
    @CollectionTable(name = "tb_incubadora_abrangencia", joinColumns = @JoinColumn(name = "incubadora_id"))
    @Column(name = "abrangencia", length = 120)
    private List<String> abrangencia = new ArrayList<>();

    @Column(name = "modalidade", length = 60)
    private String modalidade;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "link", length = 500)
    private String link;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public Incubadora() {
        this.id = UUID.randomUUID();
        this.dataCadastro = LocalDateTime.now(java.time.ZoneId.systemDefault());
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getFoco() {
        return foco;
    }
    public void setFoco(String foco) {
        this.foco = foco;
    }
    public String getCriteriosEntrada() {
        return criteriosEntrada;
    }
    public void setCriteriosEntrada(String criteriosEntrada) {
        this.criteriosEntrada = criteriosEntrada;
    }
    public String getLocalidade() {
        return localidade;
    }
    public void setLocalidade(String localidade) {
        this.localidade = localidade;
    }
    public List<String> getAbrangencia() {
        return abrangencia;
    }
    public void setAbrangencia(List<String> abrangencia) {
        this.abrangencia = abrangencia;
    }
    public String getModalidade() {
        return modalidade;
    }
    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }
    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public boolean isAtivo() {
        return ativo;
    }
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}