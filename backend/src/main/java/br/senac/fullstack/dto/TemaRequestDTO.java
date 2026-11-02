package br.senac.fullstack.dto;

import br.senac.fullstack.model.TipoVocabulario;
import jakarta.validation.constraints.NotBlank;

public record TemaRequestDTO(
        @NotBlank String nome,
        String descricao,
        Boolean ativo,
        TipoVocabulario tipo
) {
    public TemaRequestDTO(String nome, String descricao, Boolean ativo) {
        this(nome, descricao, ativo, TipoVocabulario.TEMA);
    }
}
