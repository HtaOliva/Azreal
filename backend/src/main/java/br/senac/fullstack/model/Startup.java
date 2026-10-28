package br.senac.fullstack.model;

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
        this.dataCadastro = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getAreaAtuacao() { return areaAtuacao; }
    public void setAreaAtuacao(String areaAtuacao) { this.areaAtuacao = areaAtuacao; }
    public LocalDate getDataFundacao() { return dataFundacao; }
    public void setDataFundacao(LocalDate dataFundacao) { this.dataFundacao = dataFundacao; }
    public Estagio getEstagioAtual() { return estagioAtual; }
    public void setEstagioAtual(Estagio estagioAtual) { this.estagioAtual = estagioAtual; }
    public String getLocalidadeSede() { return localidadeSede; }
    public void setLocalidadeSede(String localidadeSede) { this.localidadeSede = localidadeSede; }
    public String getLocalidadeAtuacao() { return localidadeAtuacao; }
    public void setLocalidadeAtuacao(String localidadeAtuacao) { this.localidadeAtuacao = localidadeAtuacao; }
    public boolean isLigadaIncubadora() { return ligadaIncubadora; }
    public void setLigadaIncubadora(boolean ligadaIncubadora) { this.ligadaIncubadora = ligadaIncubadora; }
    public String getIncubadoraLigada() { return incubadoraLigada; }
    public void setIncubadoraLigada(String incubadoraLigada) { this.incubadoraLigada = incubadoraLigada; }
    public List<String> getIntegrantes() { return integrantes; }
    public void setIntegrantes(List<String> integrantes) { this.integrantes = integrantes; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}