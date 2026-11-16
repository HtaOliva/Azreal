package br.senac.fullstack.dto;

import br.senac.fullstack.model.TipoVocabulario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeraisDTOTest {

    @Test
    void aceiteTermoResponseDTOConstrutorLegado() {
        UUID id = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();
        AceiteTermoResponseDTO dto = new AceiteTermoResponseDTO(id, "v1.0", agora);

        assertEquals(id, dto.id());
        assertEquals("v1.0", dto.versaoTermo());
        assertEquals(agora, dto.dataHora());
        assertTrue(dto.consentimentoIaAnonimizado());
    }

    @Test
    void temaResponseDTOConstrutorLegado() {
        UUID id = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();
        TemaResponseDTO dto = new TemaResponseDTO(id, "Saude", "Descricao", true, agora);

        assertEquals(id, dto.id());
        assertEquals("Saude", dto.nome());
        assertEquals("Descricao", dto.descricao());
        assertTrue(dto.ativo());
        assertEquals(agora, dto.dataCadastro());
        assertEquals(TipoVocabulario.TEMA, dto.tipo());
    }
}
