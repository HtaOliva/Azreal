package br.senac.fullstack.dto;

import br.senac.fullstack.model.PorteOrganizacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record AmbienteValidacaoResponseDTO(
        UUID id,
        String razaoSocial,
        String nomeFantasia,
        String setor,
        PorteOrganizacao porte,
        String ufIbge,
        String municipioIbge,
        String emailContato,
        String telefoneContato,
        String linkedinUrl,
        boolean ativo,
        LocalDateTime dataCadastro
) {
}
