package br.senac.fullstack.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaliacaoMatchTest {

    @Test
    @DisplayName("Instanciacao com valores padrao de AvaliacaoMatch")
    void instanciacaoComValoresPadrao() {
        AvaliacaoMatch avaliacao = new AvaliacaoMatch();

        assertNotNull(avaliacao.getId());
        assertNotNull(avaliacao.getDataCriacao());
        assertEquals(OrigemMatch.IA, avaliacao.getOrigemMatch());
    }

    @Test
    @DisplayName("Atribuicao e consulta de todos os campos")
    void atribuicaoEConsultaCampos() {
        AvaliacaoMatch avaliacao = new AvaliacaoMatch();
        UUID id = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID termoId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        avaliacao.setId(id);
        avaliacao.setPropostaPocId(propostaId);
        avaliacao.setTermoPilotoId(termoId);
        avaliacao.setTipoAvaliador(TipoAvaliadorMatch.STARTUP);
        avaliacao.setUsuarioResponsavel("startup@azreal.com");
        avaliacao.setEncaixeFezSentido(true);
        avaliacao.setNotaQualidade(5);
        avaliacao.setComentario("O match foi muito preciso e o desafio atendia nosso escopo perfeitamente");
        avaliacao.setOrigemMatch(OrigemMatch.REGRAS);
        avaliacao.setDataCriacao(agora);

        assertEquals(id, avaliacao.getId());
        assertEquals(propostaId, avaliacao.getPropostaPocId());
        assertEquals(termoId, avaliacao.getTermoPilotoId());
        assertEquals(TipoAvaliadorMatch.STARTUP, avaliacao.getTipoAvaliador());
        assertEquals("startup@azreal.com", avaliacao.getUsuarioResponsavel());
        assertTrue(avaliacao.getEncaixeFezSentido());
        assertEquals(5, avaliacao.getNotaQualidade());
        assertEquals("O match foi muito preciso e o desafio atendia nosso escopo perfeitamente", avaliacao.getComentario());
        assertEquals(OrigemMatch.REGRAS, avaliacao.getOrigemMatch());
        assertEquals(agora, avaliacao.getDataCriacao());
    }

    @Test
    @DisplayName("Validacao de equals e hashCode baseados em ID")
    void validacaoEqualsEHashCode() {
        AvaliacaoMatch avaliacaoBase = new AvaliacaoMatch();
        UUID identificador = UUID.randomUUID();
        avaliacaoBase.setId(identificador);

        AvaliacaoMatch avaliacaoMesmoId = new AvaliacaoMatch();
        avaliacaoMesmoId.setId(identificador);

        AvaliacaoMatch avaliacaoOutroId = new AvaliacaoMatch();
        avaliacaoOutroId.setId(UUID.randomUUID());

        assertEquals(avaliacaoBase, avaliacaoBase);
        assertEquals(avaliacaoBase, avaliacaoMesmoId);
        assertNotEquals(avaliacaoBase, avaliacaoOutroId);
        assertNotEquals(null, avaliacaoBase);
        assertNotEquals(new Object(), avaliacaoBase);
        assertEquals(avaliacaoBase.hashCode(), avaliacaoMesmoId.hashCode());
    }
}
