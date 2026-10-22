package br.senac.fullstack.dto;

import br.senac.fullstack.model.StatusVinculo;

import java.time.LocalDateTime;
import java.util.UUID;

public record VinculoResponseDTO(
        UUID id,
        UUID startupId,
        UUID mentorId,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        StatusVinculo status,
        String regras,
        String contatoMentor
) {}
