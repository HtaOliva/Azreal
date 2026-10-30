package br.senac.fullstack.service;

import br.senac.fullstack.dto.AnotacaoRequestDTO;
import br.senac.fullstack.model.AnotacaoMentoria;
import br.senac.fullstack.repository.AnotacaoMentoriaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnotacaoServiceTest {

    private VinculoService criarVinculoService() {
        return new VinculoService(
                new NullObjectProvider<>(),
                new MentorService(new NullObjectProvider<>(), new AceiteTermoService(new NullObjectProvider<>())));
    }

    @Test
    void criarSemVinculoLanca403() {
        AnotacaoService s = new AnotacaoService(new NullObjectProvider<>(), criarVinculoService());
        assertThrows(Exception.class, () -> s.criar(new AnotacaoRequestDTO(UUID.randomUUID(), UUID.randomUUID(), "conteudo")));
    }

    @Test
    void listarPorStartupVazioEMemoriaComFiltro() {
        VinculoService vs = criarVinculoService();
        AnotacaoService s = new AnotacaoService(new NullObjectProvider<>(), vs);
        UUID startup1 = UUID.randomUUID();
        UUID startup2 = UUID.randomUUID();
        UUID mentor = UUID.randomUUID();

        assertTrue(s.listarPorStartup(startup1).isEmpty());

        vs.criar(startup1, mentor);
        vs.criar(startup2, mentor);
        s.criar(new AnotacaoRequestDTO(mentor, startup1, "Nota 1"));
        s.criar(new AnotacaoRequestDTO(mentor, startup2, "Nota 2"));

        var lista = s.listarPorStartup(startup1);
        assertEquals(1, lista.size());
        assertEquals("Nota 1", lista.get(0).conteudo());
    }

    @Test
    void criarEListarComRepositorioFuncionam() {
        VinculoService vs = criarVinculoService();
        UUID startupId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        vs.criar(startupId, mentorId);

        AnotacaoMentoriaRepository repo = mock(AnotacaoMentoriaRepository.class);
        AnotacaoMentoria entidade = new AnotacaoMentoria();
        entidade.setStartupId(startupId);
        entidade.setMentorId(mentorId);
        entidade.setConteudo("Via Repo");

        when(repo.save(any(AnotacaoMentoria.class))).thenReturn(entidade);
        when(repo.findByStartupId(startupId)).thenReturn(List.of(entidade));

        AnotacaoService s = new AnotacaoService(new FixedObjectProvider<>(repo), vs);
        var criada = s.criar(new AnotacaoRequestDTO(mentorId, startupId, "Via Repo"));
        assertNotNull(criada);
        assertFalse(s.listarPorStartup(startupId).isEmpty());
    }
}
