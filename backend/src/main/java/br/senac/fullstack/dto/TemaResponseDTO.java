package br.senac.fullstack.dto;

import br.senac.fullstack.model.TipoVocabulario;

import java.time.LocalDateTime;
import java.util.UUID;

public record TemaResponseDTO(
        UUID id,
        String nome,
        String descricao,
        boolean ativo,
        LocalDateTime dataCadastro,
        TipoVocabulario tipo
) {
    public TemaResponseDTO(UUID id, String nome, String descricao, boolean ativo, LocalDateTime dataCadastro) {
        this(id, nome, descricao, ativo, dataCadastro, TipoVocabulario.TEMA);
    }
}
