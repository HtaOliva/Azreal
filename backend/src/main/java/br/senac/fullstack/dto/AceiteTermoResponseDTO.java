package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AceiteTermoResponseDTO(
        UUID id,
        String versaoTermo,
        LocalDateTime dataHora,
        boolean consentimentoIaAnonimizado
) {
    public AceiteTermoResponseDTO(UUID id, String versaoTermo, LocalDateTime dataHora) {
        this(id, versaoTermo, dataHora, true);
    }
}
