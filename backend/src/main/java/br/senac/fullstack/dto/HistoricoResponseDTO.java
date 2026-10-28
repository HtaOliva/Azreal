package br.senac.fullstack.dto;

import java.util.List;

public record HistoricoResponseDTO(
        List<SolicitacaoResponseDTO> pedidos,
        List<ApoioBuscadoResponseDTO> apoiosBuscados,
        List<RecomendacaoResponseDTO> recomendacoes,
        List<AnotacaoResponseDTO> anotacoes,
        List<CertificadoResponseDTO> certificados
) { /* empty */ }
