package br.senac.fullstack.dto;

import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusDesafio;

import java.time.LocalDateTime;
import java.util.UUID;

public record DesafioAnonimoResponseDTO(
        UUID id,
        String titulo,
        String descricaoProblema,
        String setor,
        PorteOrganizacao porte,
        String ufIbge,
        String municipioIbge,
        ModalidadeTeste modalidade,
        String publicoAlvo,
        Integer tamanhoAmostra,
        Integer duracaoMaxima,
        StatusDesafio status,
        LocalDateTime dataCriacao
) {
    public static DesafioAnonimoResponseDTO fromEntity(DesafioValidacao d) {
        var ambiente = d.getAmbienteValidacao();
        return new DesafioAnonimoResponseDTO(
                d.getId(),
                d.getTitulo(),
                d.getDescricaoProblema(),
                ambiente != null ? ambiente.getSetor() : null,
                ambiente != null ? ambiente.getPorte() : null,
                ambiente != null ? ambiente.getUfIbge() : null,
                ambiente != null ? ambiente.getMunicipioIbge() : null,
                d.getModalidade(),
                d.getPublicoAlvo(),
                d.getTamanhoAmostra(),
                d.getDuracaoMaxima(),
                d.getStatus(),
                d.getDataCriacao()
        );
    }
}
