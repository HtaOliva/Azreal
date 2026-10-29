package br.senac.fullstack.service;

import br.senac.fullstack.dto.VinculoResponseDTO;
import br.senac.fullstack.model.VinculoMentoria;
import br.senac.fullstack.repository.VinculoMentoriaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VinculoServiceRepoTest {

    private MentorService mentorService() {
        return new MentorService(new NullObjectProvider<>(), new AceiteTermoService(new NullObjectProvider<>()));
    }

    @Test
    void criarComRepo() {
        VinculoMentoriaRepository repo = mock(VinculoMentoriaRepository.class);
        VinculoMentoria v = new VinculoMentoria();
        when(repo.save(any(VinculoMentoria.class))).thenReturn(v);
        VinculoService s = new VinculoService(new FixedObjectProvider<>(repo), mentorService());
        VinculoMentoria r = s.criar(UUID.randomUUID(), UUID.randomUUID());
        assertNotNull(r);
    }

    @Test
    void temVinculoAtivoComRepo() {
        VinculoMentoriaRepository repo = mock(VinculoMentoriaRepository.class);
        VinculoMentoria v = new VinculoMentoria();
        v.setStartupId(UUID.randomUUID());
        v.setMentorId(UUID.randomUUID());
        v.setStatus(br.senac.fullstack.model.StatusVinculo.ATIVO);
        when(repo.findByStartupId(v.getStartupId())).thenReturn(List.of(v));
        VinculoService s = new VinculoService(new FixedObjectProvider<>(repo), mentorService());
        assertTrue(s.temVinculoAtivo(v.getStartupId(), v.getMentorId()));
    }

    @Test
    void encerrarComRepoExistente() {
        VinculoMentoriaRepository repo = mock(VinculoMentoriaRepository.class);
        VinculoMentoria v = new VinculoMentoria();
        UUID id = v.getId();
        when(repo.findById(id)).thenReturn(Optional.of(v));
        when(repo.save(any(VinculoMentoria.class))).thenReturn(v);
        VinculoService s = new VinculoService(new FixedObjectProvider<>(repo), mentorService());
        assertDoesNotThrow(() -> s.encerrar(id));
    }

    @Test
    void buscarPorStartupComRepoExistente() {
        VinculoMentoriaRepository repo = mock(VinculoMentoriaRepository.class);
        VinculoMentoria v = new VinculoMentoria();
        UUID startupId = UUID.randomUUID();
        v.setStartupId(startupId);
        v.setMentorId(UUID.randomUUID());
        when(repo.findByStartupId(startupId)).thenReturn(List.of(v));
        VinculoService s = new VinculoService(new FixedObjectProvider<>(repo), mentorService());
        VinculoResponseDTO r = s.buscarPorStartup(startupId);
        assertNotNull(r);
    }
}
