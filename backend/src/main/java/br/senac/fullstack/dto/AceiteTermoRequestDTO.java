package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;

public record AceiteTermoRequestDTO(
        @NotBlank String versaoTermo,
        String ip
) { /* empty */ }
