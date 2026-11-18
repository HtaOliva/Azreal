package br.senac.fullstack.dto;

import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.ModoPrivacidadePoc;
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
        AmbienteValidacaoResponseDTO identidadeAmbienteRevelada,
        String resumoEntrega,
        LocalDateTime dataEntrega,
        Boolean pilotoExecutado,
        AvaliacaoMetrica avaliacaoMetrica,
        String parecer,
        ModoPrivacidadePoc modoPrivacidade,
        Boolean certificadoEmitido,
        LocalDateTime dataConfirmacao
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
                setor, porte, null, null, null, null, null, null, null, false, null);
    }

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
            PorteOrganizacao porte,
            AmbienteValidacaoResponseDTO identidadeAmbienteRevelada
    ) {
        this(id, propostaPocId, startupId, desafioValidacaoId, ambienteValidacaoId, escopoTeste,
                duracaoDias, responsabilidadesStartup, responsabilidadesAmbiente, metricaValidacao,
                dataCriacao, statusProposta, razaoSocial, nomeFantasia, emailContato, telefoneContato,
                setor, porte, identidadeAmbienteRevelada, null, null, null, null, null, null, false, null);
    }

    public static TermoPilotoResponseDTO fromEntity(
            TermoPiloto termo,
            StatusProposta status,
            AmbienteValidacaoResponseDTO ambiente) {
        if (termo == null) {
            return null;
        }
        StatusProposta statusEfetivo = status != null ? status
                : (termo.getStatus() != null ? termo.getStatus() : StatusProposta.EM_TESTE);

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
                statusEfetivo,
                ambiente != null ? ambiente.razaoSocial() : null,
                ambiente != null ? ambiente.nomeFantasia() : null,
                ambiente != null ? ambiente.emailContato() : null,
                ambiente != null ? ambiente.telefoneContato() : null,
                ambiente != null ? ambiente.setor() : null,
                ambiente != null ? ambiente.porte() : null,
                ambiente,
                termo.getResumoEntrega(),
                termo.getDataEntrega(),
                termo.getPilotoExecutado(),
                termo.getAvaliacaoMetrica(),
                termo.getParecer(),
                termo.getModoPrivacidade(),
                termo.getCertificadoEmitido() != null ? termo.getCertificadoEmitido() : false,
                termo.getDataConfirmacao()
        );
    }

    public static TermoPilotoResponseDTO fromEntity(
            TermoPiloto termo,
            AmbienteValidacaoResponseDTO ambiente) {
        return fromEntity(termo, termo != null ? termo.getStatus() : StatusProposta.EM_TESTE, ambiente);
    }
}
