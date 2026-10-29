package br.senac.fullstack.service;

import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.model.Estagio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StartupServiceTest {

    private final AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());

    private StartupService service() {
        return new StartupService(new NullObjectProvider<>(), aceiteService);
    }

    private UUID aceiteId() {
        return aceiteService.registrar(new br.senac.fullstack.dto.AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
    }

    @Test
    void criarSemAceiteLancaBadRequest() {
        StartupService s = service();
        StartupRequestDTO req = new StartupRequestDTO("Startup", "s@e.com", "senha123", "desc", "area", LocalDate.now(), Estagio.IDEACAO, null, null, null, null, null, null);
        assertThrows(Exception.class, () -> s.criar(req));
    }

    @Test
    void criarComAceiteRetornaStartup() {
        StartupService s = service();
        UUID aid = aceiteId();
        StartupRequestDTO req = new StartupRequestDTO("Startup", "s@e.com", "senha123", "desc", "area", LocalDate.now(), Estagio.IDEACAO, null, null, null, null, null, aid);
        StartupResponseDTO r = s.criar(req);
        assertNotNull(r);
        assertNotNull(r.id());
        assertEquals("Startup", r.nome());
    }

    @Test
    void buscarInexistenteLanca404() {
        StartupService s = service();
        assertThrows(Exception.class, () -> s.buscar(UUID.randomUUID()));
    }

    @Test
    void listarVazio() {
        StartupService s = service();
        assertTrue(s.listar().isEmpty());
    }

    @Test
    void atualizarInexistenteLanca404() {
        StartupService s = service();
        assertThrows(Exception.class, () -> s.atualizar(UUID.randomUUID(), new br.senac.fullstack.dto.StartupUpdateDTO(null, null, null, null, null, null, null, null, null)));
    }
}
