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

class RegistroOficialPocTest {

    @Test
    @DisplayName("Instanciacao com valores padrao e geracao automatica de codigo verificador")
    void instanciacaoComValoresPadrao() {
        RegistroOficialPoc registro = new RegistroOficialPoc();

        assertNotNull(registro.getId());
        assertNotNull(registro.getCodigoVerificador());
        assertTrue(registro.getCodigoVerificador().startsWith("POC-"));
        assertNotNull(registro.getDataEmissao());
    }

    @Test
    @DisplayName("Atribuicao e consulta de todos os campos imutaveis")
    void atribuicaoEConsultaDeCampos() {
        RegistroOficialPoc registro = new RegistroOficialPoc();
        UUID id = UUID.randomUUID();
        UUID termoId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        registro.setId(id);
        registro.setCodigoVerificador("POC-2026-TEST1234");
        registro.setTermoPilotoId(termoId);
        registro.setPropostaPocId(propostaId);
        registro.setStartupId(startupId);
        registro.setAmbienteValidacaoId(ambienteId);
        registro.setDesafioValidacaoId(desafioId);
        registro.setEscopoTestado("Integracao de sensores IoT");
        registro.setDuracaoDias(25);
        registro.setAvaliacaoMetrica(AvaliacaoMetrica.ATENDEU_TOTALMENTE);
        registro.setParecer("Excelente reducao de latencia e dados consistentes");
        registro.setModoPrivacidade(ModoPrivacidadePoc.EXIBICAO_NOMINAL);
        registro.setDirecionamentoPosPoc("Contratacao piloto");
        registro.setMetricaValidacao("Latencia < 150ms");
        registro.setResumoEntrega("50 sensores instalados");
        registro.setNomeOrganizacao("Hospital Albert Einstein");
        registro.setSetorOrganizacao("Saude");
        registro.setPorteOrganizacao(PorteOrganizacao.GRANDE);
        registro.setDataEmissao(agora);

        assertEquals(id, registro.getId());
        assertEquals("POC-2026-TEST1234", registro.getCodigoVerificador());
        assertEquals(termoId, registro.getTermoPilotoId());
        assertEquals(propostaId, registro.getPropostaPocId());
        assertEquals(startupId, registro.getStartupId());
        assertEquals(ambienteId, registro.getAmbienteValidacaoId());
        assertEquals(desafioId, registro.getDesafioValidacaoId());
        assertEquals("Integracao de sensores IoT", registro.getEscopoTestado());
        assertEquals(25, registro.getDuracaoDias());
        assertEquals(AvaliacaoMetrica.ATENDEU_TOTALMENTE, registro.getAvaliacaoMetrica());
        assertEquals("Excelente reducao de latencia e dados consistentes", registro.getParecer());
        assertEquals(ModoPrivacidadePoc.EXIBICAO_NOMINAL, registro.getModoPrivacidade());
        assertEquals("Contratacao piloto", registro.getDirecionamentoPosPoc());
        assertEquals("Latencia < 150ms", registro.getMetricaValidacao());
        assertEquals("50 sensores instalados", registro.getResumoEntrega());
        assertEquals("Hospital Albert Einstein", registro.getNomeOrganizacao());
        assertEquals("Saude", registro.getSetorOrganizacao());
        assertEquals(PorteOrganizacao.GRANDE, registro.getPorteOrganizacao());
        assertEquals(agora, registro.getDataEmissao());
    }

    @Test
    @DisplayName("RF20 - Emitir Registro Oficial de POC com modo EXIBICAO_NOMINAL")
    void emitirRegistroComModoNominal() {
        TermoPiloto termo = new TermoPiloto();
        termo.setPropostaPocId(UUID.randomUUID());
        termo.setStartupId(UUID.randomUUID());
        termo.setAmbienteValidacaoId(UUID.randomUUID());
        termo.setDesafioValidacaoId(UUID.randomUUID());
        termo.setEscopoTeste("Modulo de triagem automatica");
        termo.setDuracaoDias(30);
        termo.setMetricaValidacao("Acuracia > 90%");
        termo.setResumoEntrega("Entrega realizada em 3 alas do hospital");
        termo.setStatus(StatusProposta.EM_TESTE);

        termo.confirmarExecucao(
                true,
                AvaliacaoMetrica.ATENDEU_TOTALMENTE,
                "Acuracia alcancou 94.5%",
                ModoPrivacidadePoc.EXIBICAO_NOMINAL,
                "Interesse em compra de licencas",
                null
        );

        RegistroOficialPoc registro = RegistroOficialPoc.emitir(
                termo, "Hospital Santa Joana S.A.", "Saude", PorteOrganizacao.GRANDE);

        assertNotNull(registro);
        assertNotNull(registro.getId());
        assertNotNull(registro.getCodigoVerificador());
        assertEquals(termo.getId(), registro.getTermoPilotoId());
        assertEquals(termo.getStartupId(), registro.getStartupId());
        assertEquals(termo.getAmbienteValidacaoId(), registro.getAmbienteValidacaoId());
        assertEquals("Modulo de triagem automatica", registro.getEscopoTestado());
        assertEquals(30, registro.getDuracaoDias());
        assertEquals(AvaliacaoMetrica.ATENDEU_TOTALMENTE, registro.getAvaliacaoMetrica());
        assertEquals("Acuracia alcancou 94.5%", registro.getParecer());
        assertEquals(ModoPrivacidadePoc.EXIBICAO_NOMINAL, registro.getModoPrivacidade());
        assertEquals("Hospital Santa Joana S.A.", registro.getNomeOrganizacao());
        assertEquals("Saude", registro.getSetorOrganizacao());
        assertEquals(PorteOrganizacao.GRANDE, registro.getPorteOrganizacao());
    }

    @Test
    @DisplayName("RF20 - Emitir Registro Oficial de POC com modo ANONIMO_QUALIFICADO oculta nome da organizacao")
    void emitirRegistroComModoAnonimoQualificado() {
        TermoPiloto termo = new TermoPiloto();
        termo.setPropostaPocId(UUID.randomUUID());
        termo.setStartupId(UUID.randomUUID());
        termo.setAmbienteValidacaoId(UUID.randomUUID());
        termo.setDesafioValidacaoId(UUID.randomUUID());
        termo.setEscopoTeste("Algoritmo preditivo de fraude");
        termo.setDuracaoDias(15);
        termo.setMetricaValidacao("F1-Score > 0.85");
        termo.setStatus(StatusProposta.EM_TESTE);

        termo.confirmarExecucao(
                true,
                AvaliacaoMetrica.ATENDEU_PARCIALMENTE,
                "Modelo bom, necessita ajustes pontuais",
                ModoPrivacidadePoc.ANONIMO_QUALIFICADO,
                "Nova rodada de testes planejada",
                null
        );

        RegistroOficialPoc registro = RegistroOficialPoc.emitir(
                termo, "Banco Confidencial S.A.", "Financeiro", PorteOrganizacao.MEDIO);

        assertNotNull(registro);
        assertNull(registro.getNomeOrganizacao());
        assertEquals("Financeiro", registro.getSetorOrganizacao());
        assertEquals(PorteOrganizacao.MEDIO, registro.getPorteOrganizacao());
        assertEquals(ModoPrivacidadePoc.ANONIMO_QUALIFICADO, registro.getModoPrivacidade());
    }

    @Test
    @DisplayName("RF20 - Validacoes ao tentar emitir registro com termo invalido ou nao concluido")
    void emitirComTermoInvalidoDeveFalhar() {
        assertThrows(IllegalArgumentException.class, () -> RegistroOficialPoc.emitir(null, "Org", "TI", null));

        TermoPiloto termoNaoConcluido = new TermoPiloto();
        termoNaoConcluido.setStatus(StatusProposta.EM_TESTE);
        assertThrows(IllegalStateException.class,
                () -> RegistroOficialPoc.emitir(termoNaoConcluido, "Org", "TI", null));

        TermoPiloto termoNaoConfirmado = new TermoPiloto();
        termoNaoConfirmado.setStatus(StatusProposta.POC_NAO_CONFIRMADA);
        assertThrows(IllegalStateException.class,
                () -> RegistroOficialPoc.emitir(termoNaoConfirmado, "Org", "TI", null));
    }

    @Test
    @DisplayName("Equals e hashCode baseados no ID")
    void equalsEHashCode() {
        UUID id = UUID.randomUUID();
        RegistroOficialPoc r1 = new RegistroOficialPoc();
        r1.setId(id);

        RegistroOficialPoc r2 = new RegistroOficialPoc();
        r2.setId(id);

        RegistroOficialPoc r3 = new RegistroOficialPoc();
        r3.setId(UUID.randomUUID());

        assertTrue(r1.equals(r1));
        assertTrue(r1.equals(r2));
        assertFalse(r1.equals(r3));
        assertFalse(r1.equals(null));
        assertFalse(r1.equals("outro"));
        assertEquals(r1.hashCode(), r2.hashCode());
        assertEquals(r1.hashCode(), r3.hashCode());
    }
}
