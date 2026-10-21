package br.senac.fullstack.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AceiteTermo {

    private UUID id;
    private UUID usuarioId;
    private String versaoTermo;
    private LocalDateTime dataHora;
    private String ip;

    public AceiteTermo() {
        this.id = UUID.randomUUID();
        this.dataHora = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public String getVersaoTermo() { return versaoTermo; }
    public void setVersaoTermo(String versaoTermo) { this.versaoTermo = versaoTermo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
