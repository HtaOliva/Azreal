package br.senac.fullstack.dto;

import java.util.List;

public record HistoricoResponseDTO(
        List<SolicitacaoResponseDTO> pedidos,
        List<RecomendacaoResponseDTO> recomendacoes,
        List<AnotacaoResponseDTO> anotacoes
) { /* empty */ }