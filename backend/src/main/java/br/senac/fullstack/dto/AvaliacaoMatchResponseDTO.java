package br.senac.fullstack.dto;

import br.senac.fullstack.model.AvaliacaoMatch;
import br.senac.fullstack.model.OrigemMatch;
import br.senac.fullstack.model.TipoAvaliadorMatch;

import java.time.LocalDateTime;
import java.util.UUID;

public record AvaliacaoMatchResponseDTO(
        UUID id,
        UUID propostaPocId,
        UUID termoPilotoId,
        TipoAvaliadorMatch tipoAvaliador,
        String usuarioResponsavel,
        Boolean encaixeFezSentido,
        Integer notaQualidade,
        String comentario,
        OrigemMatch origemMatch,
        LocalDateTime dataCriacao
) {
    public static AvaliacaoMatchResponseDTO fromEntity(AvaliacaoMatch entity) {
        if (entity == null) {
            return null;
        }
        return new AvaliacaoMatchResponseDTO(
                entity.getId(),
                entity.getPropostaPocId(),
                entity.getTermoPilotoId(),
                entity.getTipoAvaliador(),
                entity.getUsuarioResponsavel(),
                entity.getEncaixeFezSentido(),
                entity.getNotaQualidade(),
                entity.getComentario(),
                entity.getOrigemMatch(),
                entity.getDataCriacao()
        );
    }
}
