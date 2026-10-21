package br.senac.fullstack.dto;

import br.senac.fullstack.model.Estagio;

public record StartupUpdateDTO(
        String nome,
        String descricao,
        String areaAtuacao,
        Estagio estagioAtual,
        String situacao
) {}
