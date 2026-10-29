package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.MentorRequestDTO;
import br.senac.fullstack.dto.MentorResponseDTO;
import br.senac.fullstack.dto.MentorUpdateDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MentorServiceTest {

    private final AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());

    private MentorService service() {
        return new MentorService(new NullObjectProvider<>(), aceiteService);
    }

    private UUID aceiteId() {
        return aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
    }

    @Test
    void criarSemAceiteLancaBadRequest() {
        MentorService s = service();
        MentorRequestDTO req = new MentorRequestDTO("Mentor", "m@e.com", "senha123", List.of("Java"), List.of(), "exp", "disp", null, null, null, null);
        assertThrows(Exception.class, () -> s.criar(req));
    }

    @Test
    void criarComAceiteRetornaMentor() {
        MentorService s = service();
        UUID aid = aceiteId();
        MentorRequestDTO req = new MentorRequestDTO("Mentor", "m@e.com", "senha123", List.of("Java"), List.of(), "exp", "disp", null, null, null, aid);
        MentorResponseDTO r = s.criar(req);
        assertNotNull(r);
        assertNotNull(r.id());
        assertEquals("Mentor", r.nome());
    }

    @Test
    void buscarInexistenteLanca404() {
        MentorService s = service();
        assertThrows(Exception.class, () -> s.buscar(UUID.randomUUID()));
    }

    @Test
    void listarVazio() {
        MentorService s = service();
        assertTrue(s.listar().isEmpty());
    }

    @Test
    void atualizarInexistenteLanca404() {
        MentorService s = service();
        assertThrows(Exception.class, () -> s.atualizar(UUID.randomUUID(), new MentorUpdateDTO(null, null, null, null, null, null, null, null)));
    }

    @Test
    void exigirPerfilCompletoInexistenteLanca() {
        MentorService s = service();
        assertThrows(Exception.class, () -> s.exigirPerfilCompleto(UUID.randomUUID()));
    }
}
