package br.senac.fullstack.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Mentor {

    private UUID id;
    private String nome;
    private String email;
    private List<String> areasAtuacao;
    private String disponibilidade;
    private UUID usuarioId;
    private LocalDateTime dataCadastro;

    public Mentor() {
        this.id = UUID.randomUUID();
        this.dataCadastro = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<String> getAreasAtuacao() { return areasAtuacao; }
    public void setAreasAtuacao(List<String> areasAtuacao) { this.areasAtuacao = areasAtuacao; }
    public String getDisponibilidade() { return disponibilidade; }
    public void setDisponibilidade(String disponibilidade) { this.disponibilidade = disponibilidade; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
