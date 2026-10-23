package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnotacaoResponseDTO(
        UUID id,
        UUID mentorId,
        UUID startupId,
        String conteudo,
        LocalDateTime dataHora
) {}
