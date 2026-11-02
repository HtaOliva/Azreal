package br.senac.fullstack.dto;

import br.senac.fullstack.model.ModalidadeTeste;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DesafioValidacaoRequestDTO(
        @NotNull UUID ambienteValidacaoId,
        @NotBlank @Size(max = 180) String titulo,
        @NotBlank String descricaoProblema,
        @Size(max = 255) String publicoAlvo,
        @Positive Integer tamanhoAmostra,
        @NotNull ModalidadeTeste modalidade,
        @Positive Integer duracaoMaxima
) {
}
