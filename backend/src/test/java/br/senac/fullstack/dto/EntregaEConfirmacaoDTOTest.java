package br.senac.fullstack.dto;

import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntregaEConfirmacaoDTOTest {

    @Test
    @DisplayName("Deve validar instanciacao de EntregaPilotoRequestDTO")
    void deveInstanciarEntregaPilotoRequestDTO() {
        EntregaPilotoRequestDTO dto = new EntregaPilotoRequestDTO(
                "Resumo completo da entrega",
                "https://link.com/evidencia",
                "Observacao adicional"
        );

        assertEquals("Resumo completo da entrega", dto.resumoEntrega());
        assertEquals("https://link.com/evidencia", dto.linksEvidencias());
        assertEquals("Observacao adicional", dto.observacoes());
    }

    @Test
    @DisplayName("Deve validar instanciacao de ConfirmacaoExecucaoRequestDTO")
    void deveInstanciarConfirmacaoExecucaoRequestDTO() {
        ConfirmacaoExecucaoRequestDTO dto = new ConfirmacaoExecucaoRequestDTO(
                true,
                AvaliacaoMetrica.ATENDEU_PARCIALMENTE,
                "Parecer positivo",
                ModoPrivacidadePoc.ANONIMO_QUALIFICADO,
                "Validado tecnicamente",
                null
        );

        assertTrue(dto.pilotoExecutado());
        assertEquals(AvaliacaoMetrica.ATENDEU_PARCIALMENTE, dto.avaliacaoMetrica());
        assertEquals("Parecer positivo", dto.parecer());
        assertEquals(ModoPrivacidadePoc.ANONIMO_QUALIFICADO, dto.modoPrivacidade());
        assertEquals("Validado tecnicamente", dto.direcionamentoPosPoc());
        assertNull(dto.justificativaNaoExecucao());
    }

    @Test
    @DisplayName("Deve validar mapeamento de ConfirmacaoExecucaoResponseDTO a partir de entidade")
    void deveMapearConfirmacaoExecucaoResponseDTO() {
        assertNull(ConfirmacaoExecucaoResponseDTO.fromEntity(null));

        TermoPiloto termo = new TermoPiloto();
        termo.setPropostaPocId(UUID.randomUUID());
        termo.setStartupId(UUID.randomUUID());
        termo.setAmbienteValidacaoId(UUID.randomUUID());
        termo.setStatus(StatusProposta.EM_TESTE);

        termo.confirmarExecucao(
                true,
                AvaliacaoMetrica.ATENDEU_TOTALMENTE,
                "Excelente resultado",
                ModoPrivacidadePoc.EXIBICAO_NOMINAL,
                "Contratacao aprovada",
                null
        );

        ConfirmacaoExecucaoResponseDTO response = ConfirmacaoExecucaoResponseDTO.fromEntity(termo);

        assertNotNull(response);
        assertEquals(termo.getId(), response.termoPilotoId());
        assertEquals(termo.getPropostaPocId(), response.propostaPocId());
        assertEquals(termo.getStartupId(), response.startupId());
        assertEquals(termo.getAmbienteValidacaoId(), response.ambienteValidacaoId());
        assertEquals(StatusProposta.POC_CONCLUIDA, response.status());
        assertTrue(response.pilotoExecutado());
        assertEquals(AvaliacaoMetrica.ATENDEU_TOTALMENTE, response.avaliacaoMetrica());
        assertEquals("Excelente resultado", response.parecer());
        assertEquals(ModoPrivacidadePoc.EXIBICAO_NOMINAL, response.modoPrivacidade());
        assertEquals("Contratacao aprovada", response.direcionamentoPosPoc());
        assertFalse(response.certificadoEmitido());
        assertNotNull(response.dataConfirmacao());
    }

    @Test
    @DisplayName("Deve validar parse flexivel de ModoPrivacidadePoc")
    void deveValidarParseModoPrivacidade() {
        assertNull(ModoPrivacidadePoc.fromString(null));
        assertNull(ModoPrivacidadePoc.fromString("  "));
        assertEquals(ModoPrivacidadePoc.EXIBICAO_NOMINAL, ModoPrivacidadePoc.fromString("EXIBICAO_NOMINAL"));
        assertEquals(ModoPrivacidadePoc.EXIBICAO_NOMINAL, ModoPrivacidadePoc.fromString("NOMINAL_ORGANIZACAO"));
        assertEquals(ModoPrivacidadePoc.EXIBICAO_NOMINAL, ModoPrivacidadePoc.fromString("nominal"));
        assertEquals(ModoPrivacidadePoc.ANONIMO_QUALIFICADO, ModoPrivacidadePoc.fromString("ANONIMO_QUALIFICADO"));
        assertEquals(ModoPrivacidadePoc.ANONIMO_QUALIFICADO,
                ModoPrivacidadePoc.fromString("ANONIMA_QUALIFICADA_SETOR_PORTE"));
        assertEquals(ModoPrivacidadePoc.ANONIMO_QUALIFICADO, ModoPrivacidadePoc.fromString("anonimo"));
    }

    @Test
    @DisplayName("Deve cobrir valores de AvaliacaoMetrica")
    void deveCobrirValoresAvaliacaoMetrica() {
        assertEquals(3, AvaliacaoMetrica.values().length);
        assertEquals(AvaliacaoMetrica.ATENDEU_TOTALMENTE, AvaliacaoMetrica.valueOf("ATENDEU_TOTALMENTE"));
        assertEquals(AvaliacaoMetrica.ATENDEU_PARCIALMENTE, AvaliacaoMetrica.valueOf("ATENDEU_PARCIALMENTE"));
        assertEquals(AvaliacaoMetrica.NAO_ATINGIU_METRICA, AvaliacaoMetrica.valueOf("NAO_ATINGIU_METRICA"));
    }
}
