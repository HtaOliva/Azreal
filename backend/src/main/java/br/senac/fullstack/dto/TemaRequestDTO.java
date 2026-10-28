package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;

public record TemaRequestDTO(
        @NotBlank String nome,
        String descricao,
        Boolean ativo
) { /* empty */ }