package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApoioBuscadoResponseDTO(
        UUID id,
        UUID startupId,
        String tipoApoio,
        LocalDateTime dataRegistro
) {}
