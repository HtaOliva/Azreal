package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TemaResponseDTO(
        UUID id,
        String nome,
        String descricao,
        boolean ativo,
        LocalDateTime dataCadastro
) {}