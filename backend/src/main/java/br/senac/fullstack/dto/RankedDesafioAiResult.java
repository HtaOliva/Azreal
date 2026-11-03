package br.senac.fullstack.dto;

import java.util.UUID;

public record RankedDesafioAiResult(
        UUID desafioId,
        String motivo
) {
}
