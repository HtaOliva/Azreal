package br.senac.fullstack.dto;

import br.senac.fullstack.model.Estagio;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record StartupRequestDTO(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 6, max = 120) String senha,
        String descricao,
        String areaAtuacao,
        LocalDate dataFundacao,
        Estagio estagioAtual,
        @NotNull UUID aceiteTermoId
) {}
