package br.senac.fullstack.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RegistroAuditoriaTest {

    @Test
    @DisplayName("Instanciacao com construtor padrao")
    void instanciacaoComConstrutorPadrao() {
        RegistroAuditoria auditoria = new RegistroAuditoria();

        assertNotNull(auditoria.getId());
        assertNotNull(auditoria.getDataHora());
        assertNull(auditoria.getUsuarioResponsavel());
        assertNull(auditoria.getAcao());
    }

    @Test
    @DisplayName("Instanciacao com construtor parametrizado")
    void instanciacaoComConstrutorParametrizado() {
        UUID propostaId = UUID.randomUUID();
        UUID termoId = UUID.randomUUID();

        RegistroAuditoria auditoria = new RegistroAuditoria(
                propostaId,
                termoId,
                StatusProposta.PENDENTE,
                StatusProposta.EM_TESTE,
                "admin@azreal.com",
                "ACEITE_TERMO",
                "Termo aceito pelo parceiro"
        );

        assertNotNull(auditoria.getId());
        assertEquals(propostaId, auditoria.getPropostaPocId());
        assertEquals(termoId, auditoria.getTermoPilotoId());
        assertEquals(StatusProposta.PENDENTE, auditoria.getStatusAnterior());
        assertEquals(StatusProposta.EM_TESTE, auditoria.getStatusNovo());
        assertEquals("admin@azreal.com", auditoria.getUsuarioResponsavel());
        assertEquals("ACEITE_TERMO", auditoria.getAcao());
        assertEquals("Termo aceito pelo parceiro", auditoria.getDetalhes());
        assertNotNull(auditoria.getDataHora());
    }

    @Test
    @DisplayName("Instanciacao com construtor parametrizado usando valores default")
    void instanciacaoComValoresDefault() {
        UUID propostaId = UUID.randomUUID();

        RegistroAuditoria auditoria = new RegistroAuditoria(
                propostaId,
                null,
                null,
                StatusProposta.PENDENTE,
                null,
                null,
                null
        );

        assertEquals("SISTEMA", auditoria.getUsuarioResponsavel());
        assertEquals("TRANSICAO_STATUS", auditoria.getAcao());
        assertNull(auditoria.getDetalhes());
    }

    @Test
    @DisplayName("Atribuicao e consulta de setters e getters")
    void atribuicaoEConsulta() {
        RegistroAuditoria auditoria = new RegistroAuditoria();
        UUID id = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID termoId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        auditoria.setId(id);
        auditoria.setPropostaPocId(propostaId);
        auditoria.setTermoPilotoId(termoId);
        auditoria.setStatusAnterior(StatusProposta.EM_TESTE);
        auditoria.setStatusNovo(StatusProposta.POC_CONCLUIDA);
        auditoria.setUsuarioResponsavel("parceiro@azreal.com");
        auditoria.setAcao("CONFIRMACAO_EXECUCAO");
        auditoria.setDetalhes("Execucao confirmada com sucesso");
        auditoria.setDataHora(agora);

        assertEquals(id, auditoria.getId());
        assertEquals(propostaId, auditoria.getPropostaPocId());
        assertEquals(termoId, auditoria.getTermoPilotoId());
        assertEquals(StatusProposta.EM_TESTE, auditoria.getStatusAnterior());
        assertEquals(StatusProposta.POC_CONCLUIDA, auditoria.getStatusNovo());
        assertEquals("parceiro@azreal.com", auditoria.getUsuarioResponsavel());
        assertEquals("CONFIRMACAO_EXECUCAO", auditoria.getAcao());
        assertEquals("Execucao confirmada com sucesso", auditoria.getDetalhes());
        assertEquals(agora, auditoria.getDataHora());
    }

    @Test
    @DisplayName("Validacao de equals e hashCode")
    void validacaoEqualsEHashCode() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        RegistroAuditoria r1 = new RegistroAuditoria();
        r1.setId(id1);

        RegistroAuditoria r2 = new RegistroAuditoria();
        r2.setId(id1);

        RegistroAuditoria r3 = new RegistroAuditoria();
        r3.setId(id2);

        assertEquals(r1, r1);
        assertEquals(r1, r2);
        assertNotEquals(r1, r3);
        assertNotEquals(r1, null);
        assertNotEquals(r1, "outra_classe");
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}
