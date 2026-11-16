package br.senac.fullstack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecusaPropostaRequestDTO(
        @NotBlank(message = "O motivo da recusa e obrigatorio")
        @Size(max = 500, message = "O motivo da recusa deve ter no maximo 500 caracteres")
        String motivoRecusa
) {
    public RecusaPropostaRequestDTO(String motivoRecusa) {
        this.motivoRecusa = motivoRecusa != null ? motivoRecusa.trim() : null;
    }
}
