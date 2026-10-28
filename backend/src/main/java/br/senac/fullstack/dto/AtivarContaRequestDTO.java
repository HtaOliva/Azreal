package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotNull;

public record AtivarContaRequestDTO(
        @NotNull Boolean ativo
) {}
