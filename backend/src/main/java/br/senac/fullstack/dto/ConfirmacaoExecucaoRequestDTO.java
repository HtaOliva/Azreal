package br.senac.fullstack.dto;

import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

/**
 * Payload para confirmacao de execucao do piloto pelo parceiro (RF19).
 */
public record ConfirmacaoExecucaoRequestDTO(
        @NotNull(message = "A confirmacao se o piloto foi executado e obrigatoria")
        @JsonAlias({"executado", "confirmado", "testeExecutado"})
        Boolean pilotoExecutado,

        @JsonAlias({"resultadoMetrica", "metricaAvaliada"})
        AvaliacaoMetrica avaliacaoMetrica,

        @JsonAlias({"avaliacaoTextual", "parecerTecnico", "feedback"})
        String parecer,

        @JsonAlias({"privacidadeModo", "tipoExibicao", "exibicaoAutorizada"})
        ModoPrivacidadePoc modoPrivacidade,

        @JsonAlias({"direcionamento", "proximosPassos"})
        String direcionamentoPosPoc,

        @JsonAlias({"motivoNaoExecucao", "justificativa"})
        String justificativaNaoExecucao
) {
}
