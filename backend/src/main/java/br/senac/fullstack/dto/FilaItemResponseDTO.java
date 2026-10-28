package br.senac.fullstack.dto;

import java.util.List;
import java.util.UUID;

public record FilaItemResponseDTO(
        UUID solicitacaoId,
        String startupNome,
        String startupDescricao,
        String assunto,
        List<String> integrantes
) {}
