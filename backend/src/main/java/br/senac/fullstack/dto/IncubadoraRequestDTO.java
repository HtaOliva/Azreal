package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record IncubadoraRequestDTO(
        @NotBlank String nome,
        String foco,
        String criteriosEntrada,
        String localidade,
        List<String> abrangencia,
        String modalidade,
        String descricao,
        String link,
        Boolean ativo
) {}