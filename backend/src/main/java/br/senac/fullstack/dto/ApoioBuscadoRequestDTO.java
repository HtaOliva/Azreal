package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ApoioBuscadoRequestDTO(
        @NotNull UUID startupId,
        @NotBlank String tipoApoio
) { /* empty */ }
