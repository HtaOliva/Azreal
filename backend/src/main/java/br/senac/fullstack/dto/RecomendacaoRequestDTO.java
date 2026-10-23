package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RecomendacaoRequestDTO(
        @NotNull UUID mentorId,
        @NotNull UUID startupId,
        @NotBlank String tipoApoio,
        String observacao
) {}
