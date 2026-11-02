package br.senac.fullstack.dto;

import br.senac.fullstack.model.PorteOrganizacao;

public record AmbienteValidacaoUpdateDTO(
        String razaoSocial,
        String nomeFantasia,
        String setor,
        PorteOrganizacao porte,
        String ufIbge,
        String municipioIbge,
        String emailContato,
        String telefoneContato,
        String linkedinUrl
) {
}
