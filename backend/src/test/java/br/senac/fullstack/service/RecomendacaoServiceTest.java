package br.senac.fullstack.service;

import br.senac.fullstack.dto.RecomendacaoRequestDTO;
import br.senac.fullstack.dto.RecomendacaoResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecomendacaoServiceTest {

    private VinculoService vinculoService() {
        return new VinculoService(new NullObjectProvider<>(), new MentorService(new NullObjectProvider<>(), new AceiteTermoService(new NullObjectProvider<>())));
    }

    private RecomendacaoService service() {
        return new RecomendacaoService(new NullObjectProvider<>(), vinculoService());
    }

    @Test
    void criarSemVinculoLanca403() {
        RecomendacaoService s = service();
        assertThrows(Exception.class, () -> s.criar(new RecomendacaoRequestDTO(UUID.randomUUID(), UUID.randomUUID(), "tipo", "obs")));
    }

    @Test
    void listarPorStartupVazio() {
        RecomendacaoService s = service();
        assertTrue(s.listarPorStartup(UUID.randomUUID()).isEmpty());
    }
}
