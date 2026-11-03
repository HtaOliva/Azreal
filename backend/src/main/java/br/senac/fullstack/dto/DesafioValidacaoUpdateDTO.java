package br.senac.fullstack.dto;

import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.StatusDesafio;

public record DesafioValidacaoUpdateDTO(
        String titulo,
        String descricaoProblema,
        String publicoAlvo,
        Integer tamanhoAmostra,
        ModalidadeTeste modalidade,
        Integer duracaoMaxima,
        StatusDesafio status
) {
}
