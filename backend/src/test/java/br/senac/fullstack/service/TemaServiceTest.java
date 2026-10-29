package br.senac.fullstack.service;

import br.senac.fullstack.dto.TemaRequestDTO;
import br.senac.fullstack.dto.TemaResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TemaServiceTest {

    private TemaService service() {
        return new TemaService(new NullObjectProvider<>());
    }

    @Test
    void criarRetornaTema() {
        TemaService s = service();
        TemaResponseDTO r = s.criar(new TemaRequestDTO("Tema", "desc", true));
        assertNotNull(r);
        assertNotNull(r.id());
    }

    @Test
    void listarTodosVazio() {
        TemaService s = service();
        assertTrue(s.listarTodos().isEmpty());
    }

    @Test
    void atualizarInexistenteLanca() {
        TemaService s = service();
        assertThrows(Exception.class, () -> s.atualizar(UUID.randomUUID(), new TemaRequestDTO("Tema", "desc", true)));
    }

    @Test
    void definirAtivoInexistenteLanca() {
        TemaService s = service();
        assertThrows(Exception.class, () -> s.definirAtivo(UUID.randomUUID(), true));
    }
}
