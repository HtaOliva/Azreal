package br.senac.fullstack.service;

import br.senac.fullstack.dto.RegraPlataformaRequestDTO;
import br.senac.fullstack.dto.RegraPlataformaResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        UUID idInexistente = UUID.randomUUID();
        RegraPlataformaRequestDTO dto = new RegraPlataformaRequestDTO("t", true);
        assertThrows(IllegalArgumentException.class, () -> s.atualizar(idInexistente, dto));
    }

    @Test
    void definirAtivoInexistenteLanca() {
        RegraPlataformaService s = service();
        UUID idInexistente = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> s.definirAtivo(idInexistente, true));
    }

    @Test
    void buscarAtivaSemDadosLanca() {
        RegraPlataformaService s = service();
        assertThrows(IllegalArgumentException.class, s::buscarAtiva);
    }

    @Test
    void fluxoCompletoEmMemoria() {
        RegraPlataformaService s = service();

        RegraPlataformaResponseDTO criada = s.criar(new RegraPlataformaRequestDTO("Termos de Piloto", true));
        assertNotNull(criada.id());
        assertTrue(criada.ativo());

        RegraPlataformaResponseDTO ativa = s.buscarAtiva();
        assertEquals(criada.id(), ativa.id());
        assertEquals("Termos de Piloto", ativa.texto());

        RegraPlataformaResponseDTO atualizada = s.atualizar(
                criada.id(), new RegraPlataformaRequestDTO("Termos Atualizados", true));
        assertEquals("Termos Atualizados", atualizada.texto());

        s.definirAtivo(criada.id(), false);
        assertFalse(s.listar().isEmpty());
    }
}
