package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;

public record RegraPlataformaRequestDTO(
        @NotBlank String texto,
        Boolean ativo
) {}