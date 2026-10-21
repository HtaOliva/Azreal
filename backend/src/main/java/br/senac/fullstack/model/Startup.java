package br.senac.fullstack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Startup {

    private UUID id;
    private String nome;
    private String email;
    private String descricao;
    private String areaAtuacao;
    private LocalDate dataFundacao;
    private Estagio estagioAtual;
    private String situacao;
    private UUID usuarioId;
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
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
