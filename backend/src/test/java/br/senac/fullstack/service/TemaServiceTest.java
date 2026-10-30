package br.senac.fullstack.service;

import br.senac.fullstack.dto.TemaRequestDTO;
import br.senac.fullstack.dto.TemaResponseDTO;
import br.senac.fullstack.model.Tema;
import br.senac.fullstack.repository.TemaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TemaServiceTest {

    private TemaService service() {
        return new TemaService(new NullObjectProvider<>());
    }

    @Test
    void criarRetornaTema() {
        TemaService s = service();
        TemaResponseDTO r = s.criar(new TemaRequestDTO("Tema", "desc", null));
        assertNotNull(r);
        assertNotNull(r.id());

        TemaResponseDTO mantido = s.atualizar(r.id(), new TemaRequestDTO(null, null, null));
        assertEquals("Tema", mantido.nome());
    }

    @Test
    void listarTodosVazio() {
        TemaService s = service();
        assertTrue(s.listarTodos().isEmpty());
    }

    @Test
    void atualizarInexistenteLanca() {
        TemaService s = service();
        assertThrows(Exception.class, () -> s.atualizar(UUID.randomUUID(), new TemaRequestDTO("Tema", "desc", true)));
    }

    @Test
    void definirAtivoInexistenteLanca() {
        TemaService s = service();
        assertThrows(Exception.class, () -> s.definirAtivo(UUID.randomUUID(), true));
    }

    @Test
    void operacoesComRepositorioFuncionam() {
        TemaRepository repo = mock(TemaRepository.class);
        Tema tema = new Tema();
        tema.setNome("Tema Repo");
        UUID id = tema.getId();

        when(repo.save(any(Tema.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findById(id)).thenReturn(Optional.of(tema));
        when(repo.findByAtivoTrue()).thenReturn(List.of(tema));
        when(repo.findAll()).thenReturn(List.of(tema));

        TemaService s = new TemaService(new FixedObjectProvider<>(repo));
        var criado = s.criar(new TemaRequestDTO("Tema Repo", "desc", true));
        assertNotNull(criado);

        var atualizado = s.atualizar(id, new TemaRequestDTO("Tema Repo 2", "desc 2", false));
        assertEquals("Tema Repo 2", atualizado.nome());

        s.definirAtivo(id, true);
        assertFalse(s.listarAtivos().isEmpty());
        assertFalse(s.listarTodos().isEmpty());

        UUID inexistente = UUID.randomUUID();
        when(repo.findById(inexistente)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                s.atualizar(inexistente, new TemaRequestDTO("X", null, null)));
        assertThrows(IllegalArgumentException.class, () -> s.definirAtivo(inexistente, false));
    }
}
