package br.senac.fullstack.dto;

import br.senac.fullstack.model.StatusSolicitacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record SolicitacaoResponseDTO(
        UUID id,
        UUID startupId,
        UUID mentorId,
        String assunto,
        StatusSolicitacao status,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataExpiracao
) {}
