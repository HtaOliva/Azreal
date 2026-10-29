package br.senac.fullstack.service;

import br.senac.fullstack.dto.AnotacaoRequestDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnotacaoServiceTest {

    private VinculoService vinculoService() {
        return new VinculoService(new NullObjectProvider<>(), new MentorService(new NullObjectProvider<>(), new AceiteTermoService(new NullObjectProvider<>())));
    }

    private AnotacaoService service() {
        return new AnotacaoService(new NullObjectProvider<>(), vinculoService());
    }

    @Test
    void criarSemVinculoLanca403() {
        AnotacaoService s = service();
        assertThrows(Exception.class, () -> s.criar(new AnotacaoRequestDTO(UUID.randomUUID(), UUID.randomUUID(), "conteudo")));
    }

    @Test
    void listarPorStartupVazio() {
        AnotacaoService s = service();
        assertTrue(s.listarPorStartup(UUID.randomUUID()).isEmpty());
    }
}
