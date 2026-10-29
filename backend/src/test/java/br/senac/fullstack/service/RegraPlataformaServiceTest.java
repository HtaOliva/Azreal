package br.senac.fullstack.service;

import br.senac.fullstack.dto.RegraPlataformaRequestDTO;
import br.senac.fullstack.dto.RegraPlataformaResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RegraPlataformaServiceTest {

    private RegraPlataformaService service() {
        return new RegraPlataformaService(new NullObjectProvider<>());
    }

    @Test
    void criarRetornaRegra() {
        RegraPlataformaService s = service();
        RegraPlataformaResponseDTO r = s.criar(new RegraPlataformaRequestDTO("texto", true));
        assertNotNull(r);
        assertNotNull(r.id());
    }

    @Test
    void listarVazio() {
        RegraPlataformaService s = service();
        assertTrue(s.listar().isEmpty());
    }

    @Test
    void atualizarInexistenteLanca() {
        RegraPlataformaService s = service();
        assertThrows(Exception.class, () -> s.atualizar(UUID.randomUUID(), new RegraPlataformaRequestDTO("t", true)));
    }

    @Test
    void definirAtivoInexistenteLanca() {
        RegraPlataformaService s = service();
        assertThrows(Exception.class, () -> s.definirAtivo(UUID.randomUUID(), true));
    }

    @Test
    void buscarAtivaSemDadosLanca() {
        RegraPlataformaService s = service();
        assertThrows(Exception.class, () -> s.buscarAtiva());
    }
}
