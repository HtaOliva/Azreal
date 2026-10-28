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
@Table(name = "tb_mentor")
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", length = 120, nullable = false)
    private String nome;

    @Column(name = "email", length = 180, nullable = false, unique = true)
    private String email;

    @ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
    @CollectionTable(name = "tb_mentor_area", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "area", length = 120)
    private List<String> areasAtuacao = new ArrayList<>();

    @ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
    @CollectionTable(name = "tb_mentor_tema", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "tema", length = 120)
    private List<String> temas = new ArrayList<>();

    @Column(name = "experiencia", columnDefinition = "text")
    private String experiencia;

    @Column(name = "disponibilidade", length = 120)
    private String disponibilidade;

    @Column(name = "localidade", length = 120)
    private String localidade;

    @Column(name = "telefone", length = 40)
    private String telefone;

    @Column(name = "linkedin", length = 255)
    private String linkedin;

    @Column(name = "aprovado", nullable = false)
    private boolean aprovado = false;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public Mentor() {
        this.id = UUID.randomUUID();
        this.dataCadastro = LocalDateTime.now();
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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public List<String> getAreasAtuacao() {
        return areasAtuacao;
    }
    public void setAreasAtuacao(List<String> areasAtuacao) {
        this.areasAtuacao = areasAtuacao;
    }
    public List<String> getTemas() {
        return temas;
    }
    public void setTemas(List<String> temas) {
        this.temas = temas;
    }
    public String getExperiencia() {
        return experiencia;
    }
    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }
    public String getDisponibilidade() {
        return disponibilidade;
    }
    public void setDisponibilidade(String disponibilidade) {
        this.disponibilidade = disponibilidade;
    }
    public String getLocalidade() {
        return localidade;
    }
    public void setLocalidade(String localidade) {
        this.localidade = localidade;
    }
    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    public String getLinkedin() {
        return linkedin;
    }
    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }
    public boolean isAprovado() {
        return aprovado;
    }
    public void setAprovado(boolean aprovado) {
        this.aprovado = aprovado;
    }
    public boolean isAtivo() {
        return ativo;
    }
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    public UUID getUsuarioId() {
        return usuarioId;
    }
    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public boolean isPerfilCompleto() {
        return nome != null && !nome.isBlank()
                && email != null && !email.isBlank()
                && areasAtuacao != null && !areasAtuacao.isEmpty()
                && temas != null && !temas.isEmpty()
                && experiencia != null && !experiencia.isBlank()
                && disponibilidade != null && !disponibilidade.isBlank()
                && localidade != null && !localidade.isBlank();
    }
}