package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.MentorRequestDTO;
import br.senac.fullstack.dto.MentorResponseDTO;
import br.senac.fullstack.dto.MentorUpdateDTO;
import br.senac.fullstack.model.Mentor;
import br.senac.fullstack.repository.MentorRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MentorServiceRepoTest {

    private AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());

    private UUID aceiteId() {
        return aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
    }

    @Test
    void criarComRepo() {
        MentorRepository repo = mock(MentorRepository.class);
        Mentor m = new Mentor();
        m.setNome("Mentor");
        m.setEmail("m@e.com");
        when(repo.save(any(Mentor.class))).thenReturn(m);
        MentorService s = new MentorService(new FixedObjectProvider<>(repo), aceiteService);
        MentorRequestDTO req = new MentorRequestDTO("Mentor", "m@e.com", "senha123", List.of("Java"), List.of(), "exp", "disp", null, null, null, aceiteId());
        MentorResponseDTO r = s.criar(req);
        assertNotNull(r);
    }

    @Test
    void buscarComRepoExistente() {
        MentorRepository repo = mock(MentorRepository.class);
        Mentor m = new Mentor();
        m.setNome("Mentor");
        m.setEmail("m@e.com");
        UUID id = m.getId();
        when(repo.findById(id)).thenReturn(Optional.of(m));
        MentorService s = new MentorService(new FixedObjectProvider<>(repo), aceiteService);
        MentorResponseDTO r = s.buscar(id);
        assertNotNull(r);
        assertEquals("Mentor", r.nome());
    }

    @Test
    void listarComRepo() {
        MentorRepository repo = mock(MentorRepository.class);
        Mentor m = new Mentor();
        m.setNome("Mentor");
        when(repo.findAll()).thenReturn(List.of(m));
        MentorService s = new MentorService(new FixedObjectProvider<>(repo), aceiteService);
        assertFalse(s.listar().isEmpty());
    }

    @Test
    void atualizarComRepo() {
        MentorRepository repo = mock(MentorRepository.class);
        Mentor m = new Mentor();
        m.setNome("Mentor");
        m.setEmail("m@e.com");
        UUID id = m.getId();
        when(repo.findById(id)).thenReturn(Optional.of(m));
        when(repo.save(any(Mentor.class))).thenReturn(m);
        MentorService s = new MentorService(new FixedObjectProvider<>(repo), aceiteService);
        MentorResponseDTO r = s.atualizar(id, new MentorUpdateDTO("Novo", null, null, null, null, null, null, null));
        assertNotNull(r);
    }

    @Test
    void buscarEntidadePorEmailComRepo() {
        MentorRepository repo = mock(MentorRepository.class);
        Mentor m = new Mentor();
        m.setNome("Mentor");
        m.setEmail("m@e.com");
        when(repo.findByEmail("m@e.com")).thenReturn(Optional.of(m));
        MentorService s = new MentorService(new FixedObjectProvider<>(repo), aceiteService);
        Mentor found = s.buscarEntidadePorEmail("m@e.com");
        assertNotNull(found);
    }
}
