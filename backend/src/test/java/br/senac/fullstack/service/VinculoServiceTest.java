package br.senac.fullstack.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VinculoServiceTest {

    private MentorService mentorService() {
        return new MentorService(new NullObjectProvider<>(), new AceiteTermoService(new NullObjectProvider<>()));
    }

    private VinculoService service() {
        return new VinculoService(new NullObjectProvider<>(), mentorService());
    }

    @Test
    void criarRetornaVinculo() {
        VinculoService s = service();
        var v = s.criar(UUID.randomUUID(), UUID.randomUUID());
        assertNotNull(v);
        assertNotNull(v.getId());
    }

    @Test
    void temVinculoAtivoSemRepoRetornaFalse() {
        VinculoService s = service();
        assertFalse(s.temVinculoAtivo(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void encerrarInexistenteLanca404() {
        VinculoService s = service();
        assertThrows(Exception.class, () -> s.encerrar(UUID.randomUUID()));
    }

    @Test
    void buscarPorStartupInexistenteLanca404() {
        VinculoService s = service();
        assertThrows(Exception.class, () -> s.buscarPorStartup(UUID.randomUUID()));
    }
}
