package br.senac.fullstack.dto;

import br.senac.fullstack.model.AvaliacaoMatch;
import br.senac.fullstack.model.OrigemMatch;
import br.senac.fullstack.model.TipoAvaliadorMatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaliacaoMatchDTOTest {

    @Test
    @DisplayName("Criacao de AvaliacaoMatchRequestDTO com construtores convenientes")
    void criacaoRequestDTO() {
        AvaliacaoMatchRequestDTO dto1 = new AvaliacaoMatchRequestDTO(true, 5, "Encaixe perfeito");
        assertTrue(dto1.encaixeFezSentido());
        assertEquals(5, dto1.notaQualidade());
        assertEquals("Encaixe perfeito", dto1.comentario());
        assertEquals(OrigemMatch.IA, dto1.origemMatch());
        assertNull(dto1.tipoAvaliador());

        AvaliacaoMatchRequestDTO dto2 = new AvaliacaoMatchRequestDTO(true, 4, "Bom", OrigemMatch.REGRAS);
        assertEquals(OrigemMatch.REGRAS, dto2.origemMatch());
    }

    @Test
    @DisplayName("Conversao de entidade para AvaliacaoMatchResponseDTO")
    void conversaoEntityParaResponseDTO() {
        AvaliacaoMatch avaliacao = new AvaliacaoMatch();
        UUID id = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID termoId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        avaliacao.setId(id);
        avaliacao.setPropostaPocId(propostaId);
        avaliacao.setTermoPilotoId(termoId);
        avaliacao.setTipoAvaliador(TipoAvaliadorMatch.AMBIENTE_VALIDACAO);
        avaliacao.setUsuarioResponsavel("hospital@saude.gov.br");
        avaliacao.setEncaixeFezSentido(true);
        avaliacao.setNotaQualidade(5);
        avaliacao.setComentario("Excelente resolucao");
        avaliacao.setOrigemMatch(OrigemMatch.IA);
        avaliacao.setDataCriacao(agora);

        AvaliacaoMatchResponseDTO dto = AvaliacaoMatchResponseDTO.fromEntity(avaliacao);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(propostaId, dto.propostaPocId());
        assertEquals(termoId, dto.termoPilotoId());
        assertEquals(TipoAvaliadorMatch.AMBIENTE_VALIDACAO, dto.tipoAvaliador());
        assertEquals("hospital@saude.gov.br", dto.usuarioResponsavel());
        assertTrue(dto.encaixeFezSentido());
        assertEquals(5, dto.notaQualidade());
        assertEquals("Excelente resolucao", dto.comentario());
        assertEquals(OrigemMatch.IA, dto.origemMatch());
        assertEquals(agora, dto.dataCriacao());
    }

    @Test
    @DisplayName("Conversao com entidade nula retorna nulo")
    void conversaoEntidadeNula() {
        assertNull(AvaliacaoMatchResponseDTO.fromEntity(null));
    }
}
