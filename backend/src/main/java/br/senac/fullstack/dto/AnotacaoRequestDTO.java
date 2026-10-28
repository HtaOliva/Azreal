package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AnotacaoRequestDTO(
        @NotNull UUID mentorId,
        @NotNull UUID startupId,
        @NotBlank String conteudo
) { /* empty */ }
