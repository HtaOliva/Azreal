package br.senac.fullstack.service;

import br.senac.fullstack.dto.SolicitacaoRequestDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SolicitacaoServiceTest {

    private MentorService mentorService() {
        return new MentorService(new NullObjectProvider<>(), new AceiteTermoService(new NullObjectProvider<>()));
    }

    private StartupService startupService() {
        return new StartupService(new NullObjectProvider<>(), new AceiteTermoService(new NullObjectProvider<>()));
    }

    private VinculoService vinculoService() {
        return new VinculoService(new NullObjectProvider<>(), mentorService());
    }

    private SolicitacaoService service() {
        return new SolicitacaoService(new NullObjectProvider<>(), startupService(), mentorService(), vinculoService());
    }

    @Test
    void criarSemRepositorioLanca() {
        SolicitacaoService s = service();
        SolicitacaoRequestDTO req = new SolicitacaoRequestDTO(UUID.randomUUID(), UUID.randomUUID(), "assunto");
        assertThrows(Exception.class, () -> s.criar(req));
    }

    @Test
    void filaDoMentorSemRepoRetornaVazio() {
        SolicitacaoService s = service();
        assertTrue(s.filaDoMentor(UUID.randomUUID()).isEmpty());
    }

    @Test
    void listarPorStartupSemRepoRetornaVazio() {
        SolicitacaoService s = service();
        assertTrue(s.listarPorStartup(UUID.randomUUID()).isEmpty());
    }
}
