package br.senac.fullstack.dto;

import br.senac.fullstack.model.PorteOrganizacao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AmbienteValidacaoRequestDTO(
        @NotBlank @Size(max = 180) String razaoSocial,
        @NotBlank @Size(max = 180) String nomeFantasia,
        @NotBlank @Size(max = 120) String setor,
        @NotNull PorteOrganizacao porte,
        @NotBlank @Size(max = 10) String ufIbge,
        @NotBlank @Size(max = 120) String municipioIbge,
        @NotBlank @Email @Size(max = 180) String emailContato,
        String telefoneContato,
        String linkedinUrl,
        @NotBlank @Size(min = 6, max = 120) String senha,
        @NotNull UUID aceiteTermoId
) {
}
