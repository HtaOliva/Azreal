package br.senac.fullstack.dto;

import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.RegistroOficialPoc;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para exibicao do Registro Oficial de POC no portfolio verificavel (RF20).
 * Aplica anonimizacao qualificada quando modoPrivacidade == ANONIMO_QUALIFICADO,
 * ocultando a identidade nominal da organizacao parceira e exibindo apenas setor e porte.
 */
public record RegistroOficialPocResponseDTO(
        UUID id,
        String codigoVerificador,
        UUID termoPilotoId,
        UUID propostaPocId,
        UUID startupId,
        UUID ambienteValidacaoId,
        UUID desafioValidacaoId,
        String escopoTestado,
        Integer duracaoDias,
        AvaliacaoMetrica avaliacaoMetrica,
        String parecer,
        ModoPrivacidadePoc modoPrivacidade,
        String direcionamentoPosPoc,
        String metricaValidacao,
        String resumoEntrega,
        String nomeOrganizacao,
        String setorOrganizacao,
        PorteOrganizacao porteOrganizacao,
        LocalDateTime dataEmissao,
        boolean anonimo
) {

    public static RegistroOficialPocResponseDTO fromEntity(RegistroOficialPoc registro) {
        if (registro == null) {
            return null;
        }

        boolean isAnonimo = registro.getModoPrivacidade() == ModoPrivacidadePoc.ANONIMO_QUALIFICADO;
        String nomeExibicao = isAnonimo ? "Organização Parceira Anônima" : registro.getNomeOrganizacao();

        return new RegistroOficialPocResponseDTO(
                registro.getId(),
                registro.getCodigoVerificador(),
                registro.getTermoPilotoId(),
                registro.getPropostaPocId(),
                registro.getStartupId(),
                registro.getAmbienteValidacaoId(),
                registro.getDesafioValidacaoId(),
                registro.getEscopoTestado(),
                registro.getDuracaoDias(),
                registro.getAvaliacaoMetrica(),
                registro.getParecer(),
                registro.getModoPrivacidade(),
                registro.getDirecionamentoPosPoc(),
                registro.getMetricaValidacao(),
                registro.getResumoEntrega(),
                nomeExibicao,
                registro.getSetorOrganizacao(),
                registro.getPorteOrganizacao(),
                registro.getDataEmissao(),
                isAnonimo
        );
    }
}
