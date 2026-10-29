package br.senac.fullstack.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_aceite_termo")
@Getter
@Setter
public class AceiteTermo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "versao_termo", length = 30, nullable = false)
    private String versaoTermo;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(name = "ip", length = 45)
    private String ip;

    public AceiteTermo() {
        this.id = UUID.randomUUID();
        this.dataHora = LocalDateTime.now(java.time.ZoneId.systemDefault());
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
