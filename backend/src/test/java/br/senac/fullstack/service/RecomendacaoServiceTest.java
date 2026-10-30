package br.senac.fullstack.service;

import br.senac.fullstack.dto.RecomendacaoRequestDTO;
import br.senac.fullstack.model.Recomendacao;
import br.senac.fullstack.repository.RecomendacaoRepository;
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

class RecomendacaoServiceTest {

    private VinculoService criarVinculoService() {
        return new VinculoService(
                new NullObjectProvider<>(),
                new MentorService(new NullObjectProvider<>(), new AceiteTermoService(new NullObjectProvider<>())));
    }

    @Test
    void criarSemVinculoLanca403() {
        RecomendacaoService s = new RecomendacaoService(new NullObjectProvider<>(), criarVinculoService());
        assertThrows(Exception.class, () -> s.criar(new RecomendacaoRequestDTO(UUID.randomUUID(), UUID.randomUUID(), "tipo", "obs")));
    }

    @Test
    void listarPorStartupVazioEMemoriaComFiltro() {
        VinculoService vs = criarVinculoService();
        RecomendacaoService s = new RecomendacaoService(new NullObjectProvider<>(), vs);
        UUID startup1 = UUID.randomUUID();
        UUID startup2 = UUID.randomUUID();
        UUID mentor = UUID.randomUUID();

        assertTrue(s.listarPorStartup(startup1).isEmpty());

        vs.criar(startup1, mentor);
        vs.criar(startup2, mentor);
        s.criar(new RecomendacaoRequestDTO(mentor, startup1, "CURSO", "Obs 1"));
        s.criar(new RecomendacaoRequestDTO(mentor, startup2, "EVENTO", "Obs 2"));

        var lista = s.listarPorStartup(startup1);
        assertEquals(1, lista.size());
        assertEquals("CURSO", lista.get(0).tipoApoio());
    }

    @Test
    void criarEListarComRepositorioFuncionam() {
        VinculoService vs = criarVinculoService();
        UUID startupId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        vs.criar(startupId, mentorId);

        RecomendacaoRepository repo = mock(RecomendacaoRepository.class);
        Recomendacao entidade = new Recomendacao();
        entidade.setStartupId(startupId);
        entidade.setMentorId(mentorId);
        entidade.setTipoApoio("INCUBADORA");
        entidade.setObservacao("Obs Repo");

        when(repo.save(any(Recomendacao.class))).thenReturn(entidade);
        when(repo.findByStartupId(startupId)).thenReturn(List.of(entidade));

        RecomendacaoService s = new RecomendacaoService(new FixedObjectProvider<>(repo), vs);
        var criada = s.criar(new RecomendacaoRequestDTO(mentorId, startupId, "INCUBADORA", "Obs Repo"));
        assertNotNull(criada);
        assertFalse(s.listarPorStartup(startupId).isEmpty());
    }
}
