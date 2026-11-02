package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;

public record AceiteTermoRequestDTO(
        @NotBlank String versaoTermo,
        String ip,
        Boolean consentimentoIaAnonimizado
) {
    public AceiteTermoRequestDTO(String versaoTermo, String ip) {
        this(versaoTermo, ip, Boolean.TRUE);
    }
}
