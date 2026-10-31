package br.senac.fullstack.dto;

import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModalidadeTeste;

import java.util.List;
import java.util.UUID;

public record StartupUpdateDTO(
        String nome,
        String descricao,
        String areaAtuacao,
        Estagio estagioAtual,
        List<String> integrantes,
        String localidadeSede,
        String localidadeAtuacao,
        Boolean ligadaIncubadora,
        String incubadoraLigada,
        String ufIbge,
        String municipioIbge,
        String oQuePrecisaValidar,
        List<UUID> temasIds,
        List<ModalidadeTeste> modalidadesSuportadas
) {
    public StartupUpdateDTO(
            String nome,
            String descricao,
            String areaAtuacao,
            Estagio estagioAtual,
            List<String> integrantes,
            String localidadeSede,
            String localidadeAtuacao,
            Boolean ligadaIncubadora,
            String incubadoraLigada) {
        this(nome, descricao, areaAtuacao, estagioAtual, integrantes,
                localidadeSede, localidadeAtuacao, ligadaIncubadora, incubadoraLigada,
                null, null, null, null, null);
    }
}
