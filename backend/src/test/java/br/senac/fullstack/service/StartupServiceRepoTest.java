package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.StartupUpdateDTO;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.Startup;
import br.senac.fullstack.repository.StartupRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StartupServiceRepoTest {

    private final AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());

    private UUID aceiteId() {
        return aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
    }

    @Test
    void criarComRepo() {
        StartupRepository repo = mock(StartupRepository.class);
        Startup s = new Startup();
        s.setNome("Startup");
        s.setEmail("s@e.com");
        when(repo.save(any(Startup.class))).thenReturn(s);
        StartupService svc = new StartupService(new FixedObjectProvider<>(repo), aceiteService);
        StartupRequestDTO req = new StartupRequestDTO(
                "Startup", "s@e.com", "senha123", "desc", "area",
                LocalDate.now(), Estagio.IDEACAO, null, null, null, null, null, aceiteId());
        StartupResponseDTO r = svc.criar(req);
        assertNotNull(r);
    }

    @Test
    void buscarComRepoExistenteEInexistente() {
        StartupRepository repo = mock(StartupRepository.class);
        Startup s = new Startup();
        s.setNome("Startup");
        s.setEmail("s@e.com");
        UUID id = s.getId();
        UUID inexistente = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(s));
        when(repo.findById(inexistente)).thenReturn(Optional.empty());
        StartupService svc = new StartupService(new FixedObjectProvider<>(repo), aceiteService);
        StartupResponseDTO r = svc.buscar(id);
        assertNotNull(r);
        assertEquals("Startup", r.nome());
        assertThrows(Exception.class, () -> svc.buscar(inexistente));
    }

    @Test
    void listarComRepo() {
        StartupRepository repo = mock(StartupRepository.class);
        Startup s = new Startup();
        s.setNome("Startup");
        when(repo.findAll()).thenReturn(List.of(s));
        StartupService svc = new StartupService(new FixedObjectProvider<>(repo), aceiteService);
        assertFalse(svc.listar().isEmpty());
    }

    @Test
    void atualizarComRepoExistenteEInexistente() {
        StartupRepository repo = mock(StartupRepository.class);
        Startup s = new Startup();
        s.setNome("Startup");
        s.setEmail("s@e.com");
        UUID id = s.getId();
        UUID inexistente = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(s));
        when(repo.findById(inexistente)).thenReturn(Optional.empty());
        when(repo.save(any(Startup.class))).thenReturn(s);
        StartupService svc = new StartupService(new FixedObjectProvider<>(repo), aceiteService);
        StartupUpdateDTO update = new StartupUpdateDTO(null, null, null, null, null, null, null, null, null);
        StartupResponseDTO r = svc.atualizar(id, update);
        assertNotNull(r);
        assertThrows(Exception.class, () -> svc.atualizar(inexistente, update));
    }
}
