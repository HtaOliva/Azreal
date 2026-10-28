package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotNull;

public record RespostaPedidoRequestDTO(
        @NotNull Boolean aceitar,
        String observacao
) {}
