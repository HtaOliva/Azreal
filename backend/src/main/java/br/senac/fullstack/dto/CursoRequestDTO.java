package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;

public record CursoRequestDTO(
        @NotBlank String titulo,
        String descricao,
        String url,
        Boolean ativo
) {}
