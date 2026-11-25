package br.senac.fullstack.dto;

import br.senac.fullstack.model.RegistroAuditoria;
import br.senac.fullstack.model.StatusProposta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RegistroAuditoriaDTOTest {

    @Test
    @DisplayName("Conversao de entidade para RegistroAuditoriaResponseDTO")
    void conversaoEntityParaResponseDTO() {
        RegistroAuditoria entity = new RegistroAuditoria();
        UUID id = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID termoId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        entity.setId(id);
        entity.setPropostaPocId(propostaId);
        entity.setTermoPilotoId(termoId);
        entity.setStatusAnterior(StatusProposta.EM_TESTE);
        entity.setStatusNovo(StatusProposta.POC_CONCLUIDA);
        entity.setUsuarioResponsavel("admin@azreal.com");
        entity.setAcao("CONFIRMACAO_EXECUCAO");
        entity.setDetalhes("Execucao confirmada com sucesso");
        entity.setDataHora(agora);

        RegistroAuditoriaResponseDTO dto = RegistroAuditoriaResponseDTO.fromEntity(entity);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(propostaId, dto.propostaPocId());
        assertEquals(termoId, dto.termoPilotoId());
        assertEquals(StatusProposta.EM_TESTE, dto.statusAnterior());
        assertEquals(StatusProposta.POC_CONCLUIDA, dto.statusNovo());
        assertEquals("admin@azreal.com", dto.usuarioResponsavel());
        assertEquals("CONFIRMACAO_EXECUCAO", dto.acao());
        assertEquals("Execucao confirmada com sucesso", dto.detalhes());
        assertEquals(agora, dto.dataHora());
    }

    @Test
    @DisplayName("Conversao de entidade nula retorna nulo")
    void conversaoEntidadeNula() {
        assertNull(RegistroAuditoriaResponseDTO.fromEntity(null));
    }
}
