package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AceiteTermoServiceTest {

    private AceiteTermoService service() {
        return new AceiteTermoService(new NullObjectProvider<>());
    }

    @Test
    void registrarRetornaAceite() {
        AceiteTermoService s = service();
        var r = s.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1");
        assertNotNull(r);
        assertNotNull(r.id());
    }

    @Test
    void existeSemRepoRetornaFalseParaNull() {
        AceiteTermoService s = service();
        assertFalse(s.existe(null));
    }

    @Test
    void existeAposRegistrarRetornaTrue() {
        AceiteTermoService s = service();
        UUID id = s.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
        assertTrue(s.existe(id));
    }

    @Test
    void buscarInexistenteLanca404() {
        AceiteTermoService s = service();
        assertThrows(Exception.class, () -> s.buscar(UUID.randomUUID()));
    }
}
