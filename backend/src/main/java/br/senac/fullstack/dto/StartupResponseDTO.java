package br.senac.fullstack.dto;

import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModalidadeTeste;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record StartupResponseDTO(
        UUID id,
        String nome,
        String email,
        String descricao,
        String areaAtuacao,
        LocalDate dataFundacao,
        Estagio estagioAtual,
        List<String> integrantes,
        String localidadeSede,
        String localidadeAtuacao,
        boolean ligadaIncubadora,
        String incubadoraLigada,
        LocalDateTime dataCadastro,
        String ufIbge,
        String municipioIbge,
        String oQuePrecisaValidar,
        List<UUID> temasIds,
        List<ModalidadeTeste> modalidadesSuportadas
) {
    public StartupResponseDTO(
            UUID id,
            String nome,
            String email,
            String descricao,
            String areaAtuacao,
            LocalDate dataFundacao,
            Estagio estagioAtual,
            List<String> integrantes,
            String localidadeSede,
            String localidadeAtuacao,
            boolean ligadaIncubadora,
            String incubadoraLigada,
            LocalDateTime dataCadastro) {
        this(id, nome, email, descricao, areaAtuacao, dataFundacao, estagioAtual,
                integrantes, localidadeSede, localidadeAtuacao, ligadaIncubadora,
                incubadoraLigada, dataCadastro, null, null, null, List.of(), List.of());
    }
}
