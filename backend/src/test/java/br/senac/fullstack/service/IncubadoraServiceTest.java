package br.senac.fullstack.service;

import br.senac.fullstack.dto.IncubadoraRequestDTO;
import br.senac.fullstack.dto.IncubadoraResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IncubadoraServiceTest {

    private IncubadoraService service() {
        return new IncubadoraService(new NullObjectProvider<>());
    }

    @Test
    void criarRetornaIncubadora() {
        IncubadoraService s = service();
        IncubadoraResponseDTO r = s.criar(new IncubadoraRequestDTO("Inc", "foco", "criterios", "local", null, "modal", "desc", "link", true));
        assertNotNull(r);
        assertNotNull(r.id());
    }

    @Test
    void listarVazio() {
        IncubadoraService s = service();
        assertTrue(s.listarTodos().isEmpty());
    }

    @Test
    void atualizarInexistenteLanca() {
        IncubadoraService s = service();
        assertThrows(Exception.class, () -> s.atualizar(UUID.randomUUID(), new IncubadoraRequestDTO("Inc", "foco", "criterios", "local", null, "modal", "desc", "link", true)));
    }

    @Test
    void definirAtivoInexistenteLanca() {
        IncubadoraService s = service();
        assertThrows(Exception.class, () -> s.definirAtivo(UUID.randomUUID(), true));
    }
}
