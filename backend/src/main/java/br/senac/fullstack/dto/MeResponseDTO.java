package br.senac.fullstack.dto;

import java.util.List;
import java.util.UUID;

public record MeResponseDTO(
        UUID id,
        String username,
        String email,
        List<String> roles,
        String perfil
) {}
