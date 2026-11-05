package br.senac.fullstack.dto;

import br.senac.fullstack.model.PropostaPoc;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record PropostaPocRequestDTO(
        @NotNull(message = "O ID da startup e obrigatorio")
        UUID startupId,

        @NotNull(message = "O ID do desafio de validacao e obrigatorio")
        UUID desafioValidacaoId,

        @NotBlank(message = "O escopo do teste e obrigatorio")
        String escopoTeste,

        @NotNull(message = "A duracao sugerida em dias e obrigatoria")
        @Positive(message = "A duracao sugerida deve ser positiva")
        Integer duracaoSugeridaDias,

        String responsabilidadesStartup,

        String responsabilidadesAmbiente,

        @NotBlank(message = "A metrica de validacao e obrigatoria")
        String metricaValidacao,

        @NotNull(message = "O aceite previo do termo de sigilo e obrigatorio")
        @AssertTrue(message = "O aceite previo do termo de sigilo e obrigatorio")
        Boolean aceitePrevioTermoSigilo
) {
    public PropostaPoc toEntity() {
        PropostaPoc entity = new PropostaPoc();
        entity.setStartupId(startupId);
        entity.setDesafioValidacaoId(desafioValidacaoId);
        entity.setEscopoTeste(escopoTeste);
        entity.setDuracaoSugeridaDias(duracaoSugeridaDias);
        entity.setResponsabilidadesStartup(responsabilidadesStartup);
        entity.setResponsabilidadesAmbiente(responsabilidadesAmbiente);
        entity.setMetricaValidacao(metricaValidacao);
        entity.setAceitePrevioTermoSigilo(Boolean.TRUE.equals(aceitePrevioTermoSigilo));
        return entity;
    }
}
