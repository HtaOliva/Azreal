package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record IncubadoraResponseDTO(
        UUID id,
        String nome,
        String foco,
        String criteriosEntrada,
        String localidade,
        List<String> abrangencia,
        String modalidade,
        String descricao,
        String link,
        boolean ativo,
        LocalDateTime dataCadastro
) { /* empty */ }