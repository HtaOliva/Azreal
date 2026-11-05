package br.senac.fullstack.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropostaPocTest {

    @Test
    void instanciacaoComValoresPadrao() {
        PropostaPoc proposta = new PropostaPoc();

        assertNotNull(proposta.getId());
        assertNotNull(proposta.getDataCriacao());
        assertEquals(StatusProposta.PENDENTE, proposta.getStatus());
        assertFalse(proposta.isAceitePrevioTermoSigilo());
    }

    @Test
    void atribuicaoEConsultaDeCampos() {
        PropostaPoc proposta = new PropostaPoc();
        UUID id = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        proposta.setId(id);
        proposta.setStartupId(startupId);
        proposta.setDesafioValidacaoId(desafioId);
        proposta.setEscopoTeste("Piloto de IA para triagem");
        proposta.setDuracaoSugeridaDias(15);
        proposta.setResponsabilidadesStartup("Instalar modelo");
        proposta.setResponsabilidadesAmbiente("Conceder acesso anonimizado");
        proposta.setMetricaValidacao("Acuracia superior a 90%");
        proposta.setAceitePrevioTermoSigilo(true);
        proposta.setStatus(StatusProposta.EM_TESTE);
        proposta.setDataCriacao(agora);

        assertEquals(id, proposta.getId());
        assertEquals(startupId, proposta.getStartupId());
        assertEquals(desafioId, proposta.getDesafioValidacaoId());
        assertEquals("Piloto de IA para triagem", proposta.getEscopoTeste());
        assertEquals(15, proposta.getDuracaoSugeridaDias());
        assertEquals("Instalar modelo", proposta.getResponsabilidadesStartup());
        assertEquals("Conceder acesso anonimizado", proposta.getResponsabilidadesAmbiente());
        assertEquals("Acuracia superior a 90%", proposta.getMetricaValidacao());
        assertTrue(proposta.isAceitePrevioTermoSigilo());
        assertEquals(StatusProposta.EM_TESTE, proposta.getStatus());
        assertEquals(agora, proposta.getDataCriacao());
    }

    @Test
    void equalsEHashCodeBaseadosNoId() {
        UUID id = UUID.randomUUID();
        PropostaPoc p1 = new PropostaPoc();
        p1.setId(id);

        PropostaPoc p2 = new PropostaPoc();
        p2.setId(id);

        PropostaPoc p3 = new PropostaPoc();
        p3.setId(UUID.randomUUID());

        boolean reflexivo = p1.equals(p1);
        boolean comparadoComNulo = p1.equals(null);

        assertTrue(reflexivo);
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertFalse(comparadoComNulo);
        assertNotEquals(p1, "outroTipo");
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void statusPropostaValoresDisponiveis() {
        assertEquals(4, StatusProposta.values().length);
        assertEquals(StatusProposta.PENDENTE, StatusProposta.valueOf("PENDENTE"));
        assertEquals(StatusProposta.EM_TESTE, StatusProposta.valueOf("EM_TESTE"));
        assertEquals(StatusProposta.RECUSADA, StatusProposta.valueOf("RECUSADA"));
        assertEquals(StatusProposta.CANCELADA, StatusProposta.valueOf("CANCELADA"));
    }
}
