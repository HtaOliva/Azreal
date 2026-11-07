package br.senac.fullstack.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload para registro de entrega do piloto pela startup (RF18).
 */
public record EntregaPilotoRequestDTO(
        @NotBlank(message = "O resumo do que foi testado e entregue e obrigatorio")
        @JsonAlias({"resumo", "descricaoEntrega", "entregasRealizadas"})
        String resumoEntrega,

        @JsonAlias({"links", "evidencias", "linkEvidencias"})
        String linksEvidencias,

        @JsonAlias({"observacoes", "detalhes"})
        String observacoes
) {
}
