package br.senac.fullstack.dto;

import br.senac.fullstack.model.PropostaPoc;
import br.senac.fullstack.model.StatusProposta;

import java.time.LocalDateTime;
import java.util.UUID;

public record PropostaPocResponseDTO(
        UUID id,
        UUID startupId,
        UUID desafioValidacaoId,
        String escopoTeste,
        Integer duracaoSugeridaDias,
        String responsabilidadesStartup,
        String responsabilidadesAmbiente,
        String metricaValidacao,
        boolean aceitePrevioTermoSigilo,
        StatusProposta status,
        LocalDateTime dataCriacao,
        LocalDateTime prazoRespostaAmbiente
) {
    public PropostaPocResponseDTO(
            UUID id,
            UUID startupId,
            UUID desafioValidacaoId,
            String escopoTeste,
            Integer duracaoSugeridaDias,
            String responsabilidadesStartup,
            String responsabilidadesAmbiente,
            String metricaValidacao,
            boolean aceitePrevioTermoSigilo,
            StatusProposta status,
            LocalDateTime dataCriacao
    ) {
        this(id, startupId, desafioValidacaoId, escopoTeste, duracaoSugeridaDias, responsabilidadesStartup,
                responsabilidadesAmbiente, metricaValidacao, aceitePrevioTermoSigilo, status, dataCriacao, null);
    }

    public static PropostaPocResponseDTO fromEntity(PropostaPoc p) {
        return new PropostaPocResponseDTO(
                p.getId(),
                p.getStartupId(),
                p.getDesafioValidacaoId(),
                p.getEscopoTeste(),
                p.getDuracaoSugeridaDias(),
                p.getResponsabilidadesStartup(),
                p.getResponsabilidadesAmbiente(),
                p.getMetricaValidacao(),
                p.isAceitePrevioTermoSigilo(),
                p.getStatus(),
                p.getDataCriacao(),
                p.getPrazoRespostaAmbiente()
        );
    }
}
