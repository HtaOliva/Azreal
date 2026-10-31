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
@Table(name = "tb_ambiente_validacao")
@Getter
@Setter
public class AmbienteValidacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "razao_social", length = 180, nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 180, nullable = false)
    private String nomeFantasia;

    @Column(name = "setor", length = 120, nullable = false)
    private String setor;

    @Enumerated(EnumType.STRING)
    @Column(name = "porte", length = 40, nullable = false)
    private PorteOrganizacao porte;

    @Column(name = "uf_ibge", length = 10, nullable = false)
    private String ufIbge;

    @Column(name = "municipio_ibge", length = 120, nullable = false)
    private String municipioIbge;

    @Column(name = "email_contato", length = 180, nullable = false, unique = true)
    private String emailContato;

    @Column(name = "telefone_contato", length = 40)
    private String telefoneContato;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public AmbienteValidacao() {
        this.id = UUID.randomUUID();
        this.dataCadastro = LocalDateTime.now(ZoneId.systemDefault());
    }
}
