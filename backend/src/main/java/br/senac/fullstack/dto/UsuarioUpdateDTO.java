package br.senac.fullstack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(
        @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres")
        String nome,

        @Email(message = "Email invalido")
        @Size(max = 180, message = "Email deve ter no maximo 180 caracteres")
        String email,

        @Size(min = 6, max = 120, message = "Senha deve ter entre 6 e 120 caracteres")
        String senha
) {
}
