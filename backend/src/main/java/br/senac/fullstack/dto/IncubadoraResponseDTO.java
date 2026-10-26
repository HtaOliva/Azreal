package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncubadoraResponseDTO(
        UUID id,
        String nome,
        String descricao,
        String link,
        boolean ativo,
        LocalDateTime dataCadastro
) {}
