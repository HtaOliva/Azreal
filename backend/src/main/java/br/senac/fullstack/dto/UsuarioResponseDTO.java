package br.senac.fullstack.dto;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email
) {
}
