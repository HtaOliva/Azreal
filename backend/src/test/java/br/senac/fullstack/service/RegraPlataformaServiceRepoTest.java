package br.senac.fullstack.service;

import br.senac.fullstack.dto.RegraPlataformaRequestDTO;
import br.senac.fullstack.dto.RegraPlataformaResponseDTO;
import br.senac.fullstack.repository.RegraPlataformaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegraPlataformaServiceRepoTest {

    @Test
    void criarComRepo() {
        RegraPlataformaRepository repo = mock(RegraPlataformaRepository.class);
        br.senac.fullstack.model.RegraPlataforma r = new br.senac.fullstack.model.RegraPlataforma();
        r.setTexto("texto");
        when(repo.save(any(br.senac.fullstack.model.RegraPlataforma.class))).thenReturn(r);
        RegraPlataformaService s = new RegraPlataformaService(new FixedObjectProvider<>(repo));
        RegraPlataformaResponseDTO resp = s.criar(new RegraPlataformaRequestDTO("texto", true));
        assertNotNull(resp);
    }

    @Test
    void listarComRepo() {
        RegraPlataformaRepository repo = mock(RegraPlataformaRepository.class);
        br.senac.fullstack.model.RegraPlataforma r = new br.senac.fullstack.model.RegraPlataforma();
        r.setTexto("texto");
        when(repo.findAll()).thenReturn(List.of(r));
        RegraPlataformaService s = new RegraPlataformaService(new FixedObjectProvider<>(repo));
        assertFalse(s.listar().isEmpty());
    }

    @Test
    void atualizarComRepo() {
        RegraPlataformaRepository repo = mock(RegraPlataformaRepository.class);
        br.senac.fullstack.model.RegraPlataforma r = new br.senac.fullstack.model.RegraPlataforma();
        r.setTexto("texto");
        UUID id = r.getId();
        when(repo.findById(id)).thenReturn(Optional.of(r));
        when(repo.save(any(br.senac.fullstack.model.RegraPlataforma.class))).thenReturn(r);
        RegraPlataformaService s = new RegraPlataformaService(new FixedObjectProvider<>(repo));
        RegraPlataformaResponseDTO resp = s.atualizar(id, new RegraPlataformaRequestDTO("novo", true));
        assertNotNull(resp);
    }

    @Test
    void definirAtivoComRepo() {
        RegraPlataformaRepository repo = mock(RegraPlataformaRepository.class);
        br.senac.fullstack.model.RegraPlataforma r = new br.senac.fullstack.model.RegraPlataforma();
        r.setTexto("texto");
        UUID id = r.getId();
        when(repo.findById(id)).thenReturn(Optional.of(r));
        when(repo.save(any(br.senac.fullstack.model.RegraPlataforma.class))).thenReturn(r);
        RegraPlataformaService s = new RegraPlataformaService(new FixedObjectProvider<>(repo));
        assertDoesNotThrow(() -> s.definirAtivo(id, false));
    }
}
