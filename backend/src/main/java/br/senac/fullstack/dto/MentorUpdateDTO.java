package br.senac.fullstack.dto;

import java.util.List;

public record MentorUpdateDTO(
        String nome,
        List<String> areasAtuacao,
        List<String> temas,
        String experiencia,
        String disponibilidade,
        String localidade,
        String telefone,
        String linkedin
) {}