package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;

public record IncubadoraRequestDTO(
        @NotBlank String nome,
        String descricao,
        String link,
        Boolean ativo
) {}
