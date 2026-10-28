package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CursoResponseDTO(
        UUID id,
        String titulo,
        String descricao,
        String url,
        boolean ativo,
        LocalDateTime dataCadastro
) { /* empty */ }
