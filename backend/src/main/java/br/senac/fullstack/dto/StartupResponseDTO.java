package br.senac.fullstack.dto;

import br.senac.fullstack.model.Estagio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record StartupResponseDTO(
        UUID id,
        String nome,
        String email,
        String descricao,
        String areaAtuacao,
        LocalDate dataFundacao,
        Estagio estagioAtual,
        String situacao,
        LocalDateTime dataCadastro
) {}
