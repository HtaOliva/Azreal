package br.senac.fullstack.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TermoPilotoTest {

    @Test
    void instanciacaoComValoresPadrao() {
        TermoPiloto termo = new TermoPiloto();

        assertNotNull(termo.getId());
        assertNotNull(termo.getDataCriacao());
        assertEquals(StatusProposta.EM_TESTE, termo.getStatus());
        assertFalse(termo.getCertificadoEmitido());
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
        assertEquals(StatusProposta.EM_TESTE, termo.getStatus());
    }

    @Test
    void congelarComPropostaNulaDeveLancarExcecao() {
        UUID ambienteId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> TermoPiloto.congelar(null, ambienteId));
    }

    @Test
    @DisplayName("RF18 - Registrar entrega com sucesso quando em teste")
    void registrarEntregaComSucesso() {
        TermoPiloto termo = new TermoPiloto();
        termo.setStatus(StatusProposta.EM_TESTE);

        termo.registrarEntrega(
                "Modelo implantado em 3 servidores com 94% de acuracia",
                "https://github.com/teste/demo",
                "Testes rodados durante 15 dias sem falhas"
        );

        assertEquals("Modelo implantado em 3 servidores com 94% de acuracia", termo.getResumoEntrega());
        assertEquals("https://github.com/teste/demo", termo.getLinksEvidencias());
        assertEquals("Testes rodados durante 15 dias sem falhas", termo.getObservacoesEntrega());
        assertNotNull(termo.getDataEntrega());
        assertEquals(StatusProposta.EM_TESTE, termo.getStatus());
    }

    @Test
    @DisplayName("RF18 - Registrar entrega com resumo em branco deve lancar IllegalArgumentException")
    void registrarEntregaComResumoEmBrancoDeveFalhar() {
        TermoPiloto termo = new TermoPiloto();
        termo.setStatus(StatusProposta.EM_TESTE);

        assertThrows(IllegalArgumentException.class, () -> termo.registrarEntrega("  ", null, null));
        assertThrows(IllegalArgumentException.class, () -> termo.registrarEntrega(null, null, null));
    }

    @Test
    @DisplayName("RF18 - Registrar entrega quando piloto nao esta EM_TESTE deve lancar IllegalStateException")
    void registrarEntregaQuandoStatusInvalidoDeveFalhar() {
        TermoPiloto termo = new TermoPiloto();
        termo.setStatus(StatusProposta.POC_CONCLUIDA);

        assertThrows(IllegalStateException.class, () -> termo.registrarEntrega("Resumo", null, null));
    }

    @Test
    @DisplayName("RF19 - Confirmar execucao com sucesso (pilotoExecutado=true)")
    void confirmarExecucaoComSucessoExecutado() {
        TermoPiloto termo = new TermoPiloto();
        termo.setStatus(StatusProposta.EM_TESTE);

        termo.confirmarExecucao(
                true,
                AvaliacaoMetrica.ATENDEU_TOTALMENTE,
                "A solucao atingiu todos os KPIs estabelecidos",
                ModoPrivacidadePoc.EXIBICAO_NOMINAL,
                "Interesse em contratacao comercial",
                null
        );

        assertEquals(StatusProposta.POC_CONCLUIDA, termo.getStatus());
        assertTrue(termo.getPilotoExecutado());
        assertEquals(AvaliacaoMetrica.ATENDEU_TOTALMENTE, termo.getAvaliacaoMetrica());
        assertEquals("A solucao atingiu todos os KPIs estabelecidos", termo.getParecer());
        assertEquals(ModoPrivacidadePoc.EXIBICAO_NOMINAL, termo.getModoPrivacidade());
        assertEquals("Interesse em contratacao comercial", termo.getDirecionamentoPosPoc());
        assertNull(termo.getJustificativaNaoExecucao());
        assertNotNull(termo.getDataConfirmacao());
        assertFalse(termo.getCertificadoEmitido());
    }

    @Test
    @DisplayName("RF19 - Confirmar quando piloto NAO foi executado (pilotoExecutado=false)")
    void confirmarExecucaoQuandoNaoExecutado() {
        TermoPiloto termo = new TermoPiloto();
        termo.setStatus(StatusProposta.EM_TESTE);

        termo.confirmarExecucao(
                false,
                null,
                "A startup nao disponibilizou a infraestrutura a tempo",
                null,
                null,
                "Cancelamento por inviabilidade tecnica"
        );

        assertEquals(StatusProposta.POC_NAO_CONFIRMADA, termo.getStatus());
        assertFalse(termo.getPilotoExecutado());
        assertFalse(termo.getCertificadoEmitido());
        assertNull(termo.getAvaliacaoMetrica());
        assertNull(termo.getModoPrivacidade());
        assertEquals("Cancelamento por inviabilidade tecnica", termo.getJustificativaNaoExecucao());
        assertNotNull(termo.getDataConfirmacao());
    }

    @Test
    @DisplayName("RF19 - Validacoes de confirmacao quando pilotoExecutado=true e faltam campos")
    void confirmarExecucaoValidacoesCamposFaltantes() {
        TermoPiloto termo = new TermoPiloto();
        termo.setStatus(StatusProposta.EM_TESTE);

        assertThrows(IllegalArgumentException.class, () -> termo.confirmarExecucao(
                null, AvaliacaoMetrica.ATENDEU_TOTALMENTE, "Parecer", ModoPrivacidadePoc.EXIBICAO_NOMINAL, null, null));

        assertThrows(IllegalArgumentException.class, () -> termo.confirmarExecucao(
                true, null, "Parecer", ModoPrivacidadePoc.EXIBICAO_NOMINAL, null, null));

        assertThrows(IllegalArgumentException.class, () -> termo.confirmarExecucao(
                true, AvaliacaoMetrica.ATENDEU_PARCIALMENTE, "  ", ModoPrivacidadePoc.EXIBICAO_NOMINAL, null, null));

        assertThrows(IllegalArgumentException.class, () -> termo.confirmarExecucao(
                true, AvaliacaoMetrica.NAO_ATINGIU_METRICA, "Parecer", null, null, null));
    }

    @Test
    @DisplayName("RF19 - Confirmar execucao quando status nao e EM_TESTE deve lancar excecao")
    void confirmarExecucaoQuandoStatusInvalido() {
        TermoPiloto termo = new TermoPiloto();
        termo.setStatus(StatusProposta.POC_NAO_CONFIRMADA);

        assertThrows(IllegalStateException.class, () -> termo.confirmarExecucao(
                true, AvaliacaoMetrica.ATENDEU_TOTALMENTE, "Parecer", ModoPrivacidadePoc.EXIBICAO_NOMINAL, null, null));
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

        boolean equalsSelf = t1.equals(t1);
        assertTrue(equalsSelf);
        assertEquals(t1, t2);
        assertNotEquals(t3, t1);
        boolean equalsNull = t1.equals(null);
        assertFalse(equalsNull);
        boolean equalsOutro = t1.equals("outroTipo");
        assertFalse(equalsOutro);
        assertEquals(t1.hashCode(), t2.hashCode());
    }
}
