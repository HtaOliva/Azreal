package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CertificadoResponseDTO(
        UUID id,
        UUID startupId,
        String nome,
        String tipo,
        Long tamanho,
        String arquivoUrl,
        LocalDateTime dataUpload
) {}
