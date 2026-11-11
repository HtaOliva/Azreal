package br.senac.fullstack.dto;

import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.RegistroOficialPoc;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO imutavel para transferencia e apresentacao do Registro Oficial de POC (RF20).
 */
public record RegistroOficialPocDTO(
        UUID id,
        String codigoVerificador,
        UUID termoPilotoId,
        UUID startupId,
        UUID ambienteValidacaoId,
        String escopoTestado,
        Integer duracaoDias,
        AvaliacaoMetrica avaliacaoMetrica,
        String parecer,
        ModoPrivacidadePoc modoPrivacidade,
        String nomeOrganizacao,
        String setorOrganizacao,
        PorteOrganizacao porteOrganizacao,
        LocalDateTime dataEmissao
) {

    public static RegistroOficialPocDTO fromEntity(RegistroOficialPoc registro) {
        if (registro == null) {
            return null;
        }
        boolean isAnonimo = registro.getModoPrivacidade() == ModoPrivacidadePoc.ANONIMO_QUALIFICADO;
        String nomeExibicao = isAnonimo ? null : registro.getNomeOrganizacao();

        return new RegistroOficialPocDTO(
                registro.getId(),
                registro.getCodigoVerificador(),
                registro.getTermoPilotoId(),
                registro.getStartupId(),
                registro.getAmbienteValidacaoId(),
                registro.getEscopoTestado(),
                registro.getDuracaoDias(),
                registro.getAvaliacaoMetrica(),
                registro.getParecer(),
                registro.getModoPrivacidade(),
                nomeExibicao,
                registro.getSetorOrganizacao(),
                registro.getPorteOrganizacao(),
                registro.getDataEmissao()
        );
    }
}
