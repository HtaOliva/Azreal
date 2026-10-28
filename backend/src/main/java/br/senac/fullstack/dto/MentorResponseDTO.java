package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MentorResponseDTO(
        UUID id,
        String nome,
        String email,
        List<String> areasAtuacao,
        List<String> temas,
        String experiencia,
        String disponibilidade,
        String localidade,
        String telefone,
        String linkedin,
        boolean perfilCompleto,
        boolean aprovado,
        LocalDateTime dataCadastro
) {}