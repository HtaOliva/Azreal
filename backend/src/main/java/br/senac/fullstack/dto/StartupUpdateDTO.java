package br.senac.fullstack.dto;

import br.senac.fullstack.model.Estagio;

import java.util.List;

public record StartupUpdateDTO(
        String nome,
        String descricao,
        String areaAtuacao,
        Estagio estagioAtual,
        List<String> integrantes,
        String localidadeSede,
        String localidadeAtuacao,
        Boolean ligadaIncubadora,
        String incubadoraLigada
) { /* empty */ }