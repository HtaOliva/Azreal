package br.senac.fullstack.dto;

import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resposta da confirmacao de execucao do piloto (RF19).
 */
public record ConfirmacaoExecucaoResponseDTO(
        UUID termoPilotoId,
        UUID propostaPocId,
        UUID startupId,
        UUID ambienteValidacaoId,
        StatusProposta status,
        Boolean pilotoExecutado,
        AvaliacaoMetrica avaliacaoMetrica,
        String parecer,
        ModoPrivacidadePoc modoPrivacidade,
        String direcionamentoPosPoc,
        String justificativaNaoExecucao,
        Boolean certificadoEmitido,
        LocalDateTime dataConfirmacao
) {

    public static ConfirmacaoExecucaoResponseDTO fromEntity(TermoPiloto termo) {
        if (termo == null) {
            return null;
        }
        return new ConfirmacaoExecucaoResponseDTO(
                termo.getId(),
                termo.getPropostaPocId(),
                termo.getStartupId(),
                termo.getAmbienteValidacaoId(),
                termo.getStatus(),
                termo.getPilotoExecutado(),
                termo.getAvaliacaoMetrica(),
                termo.getParecer(),
                termo.getModoPrivacidade(),
                termo.getDirecionamentoPosPoc(),
                termo.getJustificativaNaoExecucao(),
                termo.getCertificadoEmitido(),
                termo.getDataConfirmacao()
        );
    }
}
