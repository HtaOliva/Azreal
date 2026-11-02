package br.senac.fullstack.dto;

import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusDesafio;

import java.time.LocalDateTime;
import java.util.UUID;

public record DesafioRecomendadoDTO(
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
        LocalDateTime dataCriacao,
        String motivo,
        String origemRecomendacao
) {
    public static DesafioRecomendadoDTO de(DesafioAnonimoResponseDTO anonimo, String motivo, String origem) {
        return new DesafioRecomendadoDTO(
                anonimo.id(),
                anonimo.titulo(),
                anonimo.descricaoProblema(),
                anonimo.setor(),
                anonimo.porte(),
                anonimo.ufIbge(),
                anonimo.municipioIbge(),
                anonimo.modalidade(),
                anonimo.publicoAlvo(),
                anonimo.tamanhoAmostra(),
                anonimo.duracaoMaxima(),
                anonimo.status(),
                anonimo.dataCriacao(),
                motivo,
                origem
        );
    }

    public static DesafioRecomendadoDTO de(DesafioValidacao desafio, String motivo, String origem) {
        return de(DesafioAnonimoResponseDTO.fromEntity(desafio), motivo, origem);
    }
}
