package br.senac.fullstack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_apoio_buscado")
public class ApoioBuscado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "startup_id", nullable = false)
    private UUID startupId;

    @Column(name = "tipo_apoio", length = 120, nullable = false)
    private String tipoApoio;

    @Column(name = "data_registro", nullable = false, updatable = false)
    private LocalDateTime dataRegistro;

    public ApoioBuscado() {
        this.id = UUID.randomUUID();
        this.dataRegistro = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getStartupId() {
        return startupId;
    }
    public void setStartupId(UUID startupId) {
        this.startupId = startupId;
    }
    public String getTipoApoio() {
        return tipoApoio;
    }
    public void setTipoApoio(String tipoApoio) {
        this.tipoApoio = tipoApoio;
    }
    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }
    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }
}
