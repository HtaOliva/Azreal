package br.senac.fullstack.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AiMatchmakingExceptionTest {

    @Test
    void construtorComMensagemDeveDefinirMensagem() {
        AiMatchmakingException ex = new AiMatchmakingException("Falha na IA");
        assertEquals("Falha na IA", ex.getMessage());
    }

    @Test
    void construtorComMensagemECausaDeveDefinirAmbos() {
        RuntimeException causa = new RuntimeException("Causa raiz");
        AiMatchmakingException ex = new AiMatchmakingException("Falha na IA", causa);
        assertEquals("Falha na IA", ex.getMessage());
        assertSame(causa, ex.getCause());
    }
}
