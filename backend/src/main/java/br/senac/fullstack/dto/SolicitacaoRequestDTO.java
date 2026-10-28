package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SolicitacaoRequestDTO(
        @NotNull UUID startupId,
        @NotNull UUID mentorId,
        @NotBlank String assunto
) { /* empty */ }
