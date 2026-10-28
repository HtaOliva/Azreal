package br.senac.fullstack.dto;

import java.util.List;

public record LoginResponseDTO(
        String token,
        long expiresIn,
        List<String> roles
) {}
