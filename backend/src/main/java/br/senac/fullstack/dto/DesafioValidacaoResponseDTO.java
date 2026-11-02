package br.senac.fullstack.dto;

import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusDesafio;

import java.time.LocalDateTime;
import java.util.UUID;

public record DesafioValidacaoResponseDTO(
        UUID id,
        UUID ambienteValidacaoId,
        String ambienteValidacaoNome,
        String razaoSocial,
        String emailContato,
        String telefoneContato,
        String setor,
        PorteOrganizacao porte,
        String ufIbge,
        String municipioIbge,
        String titulo,
        String descricaoProblema,
        String publicoAlvo,
        Integer tamanhoAmostra,
        ModalidadeTeste modalidade,
        Integer duracaoMaxima,
        StatusDesafio status,
        boolean ativo,
        LocalDateTime dataCriacao
) {
    public static DesafioValidacaoResponseDTO fromEntity(DesafioValidacao d) {
        var ambiente = d.getAmbienteValidacao();
        return new DesafioValidacaoResponseDTO(
                d.getId(),
                ambiente != null ? ambiente.getId() : null,
                ambiente != null ? ambiente.getNomeFantasia() : null,
                ambiente != null ? ambiente.getRazaoSocial() : null,
                ambiente != null ? ambiente.getEmailContato() : null,
                ambiente != null ? ambiente.getTelefoneContato() : null,
                ambiente != null ? ambiente.getSetor() : null,
                ambiente != null ? ambiente.getPorte() : null,
                ambiente != null ? ambiente.getUfIbge() : null,
                ambiente != null ? ambiente.getMunicipioIbge() : null,
                d.getTitulo(),
                d.getDescricaoProblema(),
                d.getPublicoAlvo(),
                d.getTamanhoAmostra(),
                d.getModalidade(),
                d.getDuracaoMaxima(),
                d.getStatus(),
                d.isAtivo(),
                d.getDataCriacao()
        );
    }
}
