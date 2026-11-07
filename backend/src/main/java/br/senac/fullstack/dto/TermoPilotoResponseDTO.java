package br.senac.fullstack.dto;

import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TermoPilotoResponseDTO(
        UUID id,
        UUID propostaPocId,
        UUID startupId,
        UUID desafioValidacaoId,
        UUID ambienteValidacaoId,
        String escopoTeste,
        Integer duracaoDias,
        String responsabilidadesStartup,
        String responsabilidadesAmbiente,
        String metricaValidacao,
        LocalDateTime dataCriacao,
        StatusProposta statusProposta,
        String razaoSocial,
        String nomeFantasia,
        String emailContato,
        String telefoneContato,
        String setor,
        PorteOrganizacao porte,
        AmbienteValidacaoResponseDTO identidadeAmbienteRevelada
) {

    public TermoPilotoResponseDTO(
            UUID id,
            UUID propostaPocId,
            UUID startupId,
            UUID desafioValidacaoId,
            UUID ambienteValidacaoId,
            String escopoTeste,
            Integer duracaoDias,
            String responsabilidadesStartup,
            String responsabilidadesAmbiente,
            String metricaValidacao,
            LocalDateTime dataCriacao,
            StatusProposta statusProposta,
            String razaoSocial,
            String nomeFantasia,
            String emailContato,
            String telefoneContato,
            String setor,
            PorteOrganizacao porte
    ) {
        this(id, propostaPocId, startupId, desafioValidacaoId, ambienteValidacaoId, escopoTeste,
                duracaoDias, responsabilidadesStartup, responsabilidadesAmbiente, metricaValidacao,
                dataCriacao, statusProposta, razaoSocial, nomeFantasia, emailContato, telefoneContato,
                setor, porte, null);
    }

    public static TermoPilotoResponseDTO fromEntity(
            TermoPiloto termo,
            StatusProposta status,
            AmbienteValidacaoResponseDTO ambiente) {
        if (termo == null) {
            return null;
        }
        return new TermoPilotoResponseDTO(
                termo.getId(),
                termo.getPropostaPocId(),
                termo.getStartupId(),
                termo.getDesafioValidacaoId(),
                termo.getAmbienteValidacaoId(),
                termo.getEscopoTeste(),
                termo.getDuracaoDias(),
                termo.getResponsabilidadesStartup(),
                termo.getResponsabilidadesAmbiente(),
                termo.getMetricaValidacao(),
                termo.getDataCriacao(),
                status != null ? status : StatusProposta.EM_TESTE,
                ambiente != null ? ambiente.razaoSocial() : null,
                ambiente != null ? ambiente.nomeFantasia() : null,
                ambiente != null ? ambiente.emailContato() : null,
                ambiente != null ? ambiente.telefoneContato() : null,
                ambiente != null ? ambiente.setor() : null,
                ambiente != null ? ambiente.porte() : null,
                ambiente
        );
    }

    public static TermoPilotoResponseDTO fromEntity(
            TermoPiloto termo,
            AmbienteValidacaoResponseDTO ambiente) {
        return fromEntity(termo, StatusProposta.EM_TESTE, ambiente);
    }
}
