package br.senac.fullstack.dto;

import br.senac.fullstack.model.OrigemMatch;
import br.senac.fullstack.model.TipoAvaliadorMatch;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoMatchRequestDTO(
        @NotNull(message = "A indicacao se o encaixe fez sentido e obrigatoria")
        Boolean encaixeFezSentido,

        @NotNull(message = "A nota de qualidade e obrigatoria")
        @Min(value = 1, message = "A nota de qualidade minima e 1")
        @Max(value = 5, message = "A nota de qualidade maxima e 5")
        @JsonAlias({"nota", "notaMatch"})
        Integer notaQualidade,

        String comentario,

        OrigemMatch origemMatch,

        TipoAvaliadorMatch tipoAvaliador
) {
    public AvaliacaoMatchRequestDTO(Boolean encaixeFezSentido, Integer notaQualidade, String comentario) {
        this(encaixeFezSentido, notaQualidade, comentario, OrigemMatch.IA, null);
    }

    public AvaliacaoMatchRequestDTO(
            Boolean encaixeFezSentido,
            Integer notaQualidade,
            String comentario,
            OrigemMatch origemMatch) {
        this(encaixeFezSentido, notaQualidade, comentario, origemMatch, null);
    }
}
