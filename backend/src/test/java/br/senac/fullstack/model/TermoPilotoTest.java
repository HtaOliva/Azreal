package br.senac.fullstack.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TermoPilotoTest {

    @Test
    void instanciacaoComValoresPadrao() {
        TermoPiloto termo = new TermoPiloto();

        assertNotNull(termo.getId());
        assertNotNull(termo.getDataCriacao());
    }

    @Test
    void atribuicaoEConsultaDeCampos() {
        TermoPiloto termo = new TermoPiloto();
        UUID id = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        termo.setId(id);
        termo.setPropostaPocId(propostaId);
        termo.setStartupId(startupId);
        termo.setDesafioValidacaoId(desafioId);
        termo.setAmbienteValidacaoId(ambienteId);
        termo.setEscopoTeste("Escopo do piloto de telemetria");
        termo.setDuracaoDias(30);
        termo.setResponsabilidadesStartup("Entrega do SDK");
        termo.setResponsabilidadesAmbiente("Conexao com banco anonimizado");
        termo.setMetricaValidacao("Disponibilidade > 99.5%");
        termo.setDataCriacao(agora);

        assertEquals(id, termo.getId());
        assertEquals(propostaId, termo.getPropostaPocId());
        assertEquals(startupId, termo.getStartupId());
        assertEquals(desafioId, termo.getDesafioValidacaoId());
        assertEquals(ambienteId, termo.getAmbienteValidacaoId());
        assertEquals("Escopo do piloto de telemetria", termo.getEscopoTeste());
        assertEquals(30, termo.getDuracaoDias());
        assertEquals("Entrega do SDK", termo.getResponsabilidadesStartup());
        assertEquals("Conexao com banco anonimizado", termo.getResponsabilidadesAmbiente());
        assertEquals("Disponibilidade > 99.5%", termo.getMetricaValidacao());
        assertEquals(agora, termo.getDataCriacao());
    }

    @Test
    void congelarSnapshotDePropostaPocComSucesso() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        PropostaPoc proposta = new PropostaPoc();
        proposta.setStartupId(startupId);
        proposta.setDesafioValidacaoId(desafioId);
        proposta.setEscopoTeste("Validacao de modelo de NLP");
        proposta.setDuracaoSugeridaDias(20);
        proposta.setResponsabilidadesStartup("Deploy em cluster de teste");
        proposta.setResponsabilidadesAmbiente("Fornecer massa de dados");
        proposta.setMetricaValidacao("F1-score >= 0.88");

        TermoPiloto termo = TermoPiloto.congelar(proposta, ambienteId);

        assertNotNull(termo.getId());
        assertNotNull(termo.getDataCriacao());
        assertEquals(proposta.getId(), termo.getPropostaPocId());
        assertEquals(startupId, termo.getStartupId());
        assertEquals(desafioId, termo.getDesafioValidacaoId());
        assertEquals(ambienteId, termo.getAmbienteValidacaoId());
        assertEquals("Validacao de modelo de NLP", termo.getEscopoTeste());
        assertEquals(20, termo.getDuracaoDias());
        assertEquals("Deploy em cluster de teste", termo.getResponsabilidadesStartup());
        assertEquals("Fornecer massa de dados", termo.getResponsabilidadesAmbiente());
        assertEquals("F1-score >= 0.88", termo.getMetricaValidacao());
    }

    @Test
    void congelarComPropostaNulaDeveLancarExcecao() {
        UUID ambienteId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> TermoPiloto.congelar(null, ambienteId));
    }

    @Test
    void equalsEHashCodeBaseadosNoId() {
        UUID id = UUID.randomUUID();
        TermoPiloto t1 = new TermoPiloto();
        t1.setId(id);

        TermoPiloto t2 = new TermoPiloto();
        t2.setId(id);

        TermoPiloto t3 = new TermoPiloto();
        t3.setId(UUID.randomUUID());

        assertTrue(t1.equals(t1));
        assertEquals(t1, t2);
        assertNotEquals(t1, t3);
        assertFalse(t1.equals(null));
        assertFalse(t1.equals("outroTipo"));
        assertEquals(t1.hashCode(), t2.hashCode());
    }
}
