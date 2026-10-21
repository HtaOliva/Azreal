package br.senac.fullstack.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MentorResponseDTO(
        UUID id,
        String nome,
        String email,
        List<String> areasAtuacao,
        String disponibilidade,
        LocalDateTime dataCadastro
) {}
