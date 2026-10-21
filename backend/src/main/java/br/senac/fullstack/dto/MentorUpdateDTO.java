package br.senac.fullstack.dto;

import java.util.List;

public record MentorUpdateDTO(
        String nome,
        List<String> areasAtuacao,
        String disponibilidade
) {}
