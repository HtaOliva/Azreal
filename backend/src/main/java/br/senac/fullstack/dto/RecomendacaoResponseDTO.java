package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecomendacaoResponseDTO(
        UUID id,
        UUID mentorId,
        UUID startupId,
        String tipoApoio,
        String observacao,
        LocalDateTime dataHora
) { /* empty */ }
