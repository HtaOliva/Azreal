package br.senac.fullstack.service;

import br.senac.fullstack.dto.IncubadoraRequestDTO;
import br.senac.fullstack.dto.IncubadoraResponseDTO;
import br.senac.fullstack.model.Incubadora;
import br.senac.fullstack.repository.IncubadoraRepository;
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

class IncubadoraServiceTest {

    private IncubadoraService service() {
        return new IncubadoraService(new NullObjectProvider<>());
    }

    @Test
    void criarRetornaIncubadora() {
        IncubadoraService s = service();
        IncubadoraResponseDTO r = s.criar(new IncubadoraRequestDTO(
                "Inc", "foco", "criterios", "local", null, "modal", "desc", "link", null));
        assertNotNull(r);
        assertNotNull(r.id());

        // Atualizar com campos nulos mantém valores anteriores
        IncubadoraResponseDTO atualizadoNulos = s.atualizar(r.id(), new IncubadoraRequestDTO(
                null, null, null, null, null, null, null, null, null));
        assertEquals("Inc", atualizadoNulos.nome());
    }

    @Test
    void listarVazio() {
        IncubadoraService s = service();
        assertTrue(s.listarTodos().isEmpty());
    }

    @Test
    void atualizarInexistenteLanca() {
        IncubadoraService s = service();
        assertThrows(Exception.class, () -> s.atualizar(UUID.randomUUID(),
                new IncubadoraRequestDTO("Inc", "foco", "criterios", "local", null, "modal", "desc", "link", true)));
    }

    @Test
    void definirAtivoInexistenteLanca() {
        IncubadoraService s = service();
        assertThrows(Exception.class, () -> s.definirAtivo(UUID.randomUUID(), true));
    }

    @Test
    void operacoesComRepositorioFuncionam() {
        IncubadoraRepository repo = mock(IncubadoraRepository.class);
        Incubadora inc = new Incubadora();
        inc.setNome("Inc Repo");
        UUID id = inc.getId();

        when(repo.save(any(Incubadora.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findById(id)).thenReturn(Optional.of(inc));
        when(repo.findByAtivoTrue()).thenReturn(List.of(inc));
        when(repo.findAll()).thenReturn(List.of(inc));

        IncubadoraService s = new IncubadoraService(new FixedObjectProvider<>(repo));
        var criada = s.criar(new IncubadoraRequestDTO(
                "Inc Repo", "foco", "crit", "SP", List.of("BR"), "Presencial", "desc", "link", true));
        assertNotNull(criada);

        var atualizada = s.atualizar(id, new IncubadoraRequestDTO(
                "Inc Repo 2", "foco2", "crit2", "RJ", List.of("Global"), "Remoto", "desc2", "link2", false));
        assertEquals("Inc Repo 2", atualizada.nome());

        s.definirAtivo(id, true);
        assertFalse(s.listarAtivas().isEmpty());
        assertFalse(s.listarTodos().isEmpty());

        UUID inexistente = UUID.randomUUID();
        when(repo.findById(inexistente)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> s.atualizar(inexistente,
                new IncubadoraRequestDTO("X", null, null, null, null, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> s.definirAtivo(inexistente, false));
    }
}
