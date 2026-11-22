package br.senac.fullstack.dto;

import br.senac.fullstack.model.RegistroAuditoria;
import br.senac.fullstack.model.StatusProposta;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistroAuditoriaResponseDTO(
        UUID id,
        UUID propostaPocId,
        UUID termoPilotoId,
        StatusProposta statusAnterior,
        StatusProposta statusNovo,
        String usuarioResponsavel,
        String acao,
        String detalhes,
        LocalDateTime dataHora
) {
    public static RegistroAuditoriaResponseDTO fromEntity(RegistroAuditoria entity) {
        if (entity == null) {
            return null;
        }
        return new RegistroAuditoriaResponseDTO(
                entity.getId(),
                entity.getPropostaPocId(),
                entity.getTermoPilotoId(),
                entity.getStatusAnterior(),
                entity.getStatusNovo(),
                entity.getUsuarioResponsavel(),
                entity.getAcao(),
                entity.getDetalhes(),
                entity.getDataHora()
        );
    }
}
