package br.senac.fullstack.dto;

import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModalidadeTeste;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StartupRequestDTO(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 6, max = 120) String senha,
        String descricao,
        String areaAtuacao,
        LocalDate dataFundacao,
        Estagio estagioAtual,
        List<String> integrantes,
        String localidadeSede,
        String localidadeAtuacao,
        Boolean ligadaIncubadora,
        String incubadoraLigada,
        @NotNull UUID aceiteTermoId,
        String ufIbge,
        String municipioIbge,
        String oQuePrecisaValidar,
        List<UUID> temasIds,
        List<ModalidadeTeste> modalidadesSuportadas
) {
    public StartupRequestDTO(
            String nome,
            String email,
            String senha,
            String descricao,
            String areaAtuacao,
            LocalDate dataFundacao,
            Estagio estagioAtual,
            List<String> integrantes,
            String localidadeSede,
            String localidadeAtuacao,
            Boolean ligadaIncubadora,
            String incubadoraLigada,
            UUID aceiteTermoId) {
        this(nome, email, senha, descricao, areaAtuacao, dataFundacao, estagioAtual,
                integrantes, localidadeSede, localidadeAtuacao, ligadaIncubadora,
                incubadoraLigada, aceiteTermoId, null, null, null, null, null);
    }
}
