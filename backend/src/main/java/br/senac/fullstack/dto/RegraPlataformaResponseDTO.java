package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegraPlataformaResponseDTO(
        UUID id,
        String texto,
        boolean ativo,
        LocalDateTime dataAtualizacao
) {}