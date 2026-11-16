package br.senac.fullstack.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AceiteTermoTest {

    @Test
    void instanciacaoComValoresPadrao() {
        AceiteTermo aceite = new AceiteTermo();
        assertNotNull(aceite.getId());
        assertNotNull(aceite.getDataHora());
        assertTrue(aceite.isConsentimentoIaAnonimizado());
    }

    @Test
    void atribuicaoEConsultaDeCampos() {
        AceiteTermo aceite = new AceiteTermo();
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        aceite.setId(id);
        aceite.setUsuarioId(usuarioId);
        aceite.setVersaoTermo("v2.0");
        aceite.setConsentimentoIaAnonimizado(false);
        aceite.setDataHora(agora);
        aceite.setIp("192.168.1.100");

        assertEquals(id, aceite.getId());
        assertEquals(usuarioId, aceite.getUsuarioId());
        assertEquals("v2.0", aceite.getVersaoTermo());
        assertEquals(false, aceite.isConsentimentoIaAnonimizado());
        assertEquals(agora, aceite.getDataHora());
        assertEquals("192.168.1.100", aceite.getIp());
    }
}
