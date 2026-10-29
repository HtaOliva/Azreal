package br.senac.fullstack.service;

import br.senac.fullstack.dto.SolicitacaoRequestDTO;
import br.senac.fullstack.dto.SolicitacaoResponseDTO;
import br.senac.fullstack.model.SolicitacaoOrientacao;
import br.senac.fullstack.model.StatusSolicitacao;
import br.senac.fullstack.repository.SolicitacaoOrientacaoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SolicitacaoServiceRepoTest {

    @Test
    void criarComRepo() {
        SolicitacaoOrientacaoRepository repo = mock(SolicitacaoOrientacaoRepository.class);
        SolicitacaoOrientacao sol = new SolicitacaoOrientacao();
        sol.setStartupId(UUID.randomUUID());
        sol.setMentorId(UUID.randomUUID());
        when(repo.save(any(SolicitacaoOrientacao.class))).thenReturn(sol);
        StartupService startupService = mock(StartupService.class);
        MentorService mentorService = mock(MentorService.class);
        VinculoService vinculoService = mock(VinculoService.class);
        SolicitacaoService s = new SolicitacaoService(new FixedObjectProvider<>(repo), startupService, mentorService, vinculoService);
        // startup/mentor buscar will be called; stub to do nothing
        when(startupService.buscar(any())).thenReturn(null);
        when(mentorService.buscar(any())).thenReturn(null);
        SolicitacaoRequestDTO req = new SolicitacaoRequestDTO(sol.getStartupId(), sol.getMentorId(), "assunto");
        SolicitacaoResponseDTO r = s.criar(req);
        assertNotNull(r);
    }

    @Test
    void listarPorStartupComRepo() {
        SolicitacaoOrientacaoRepository repo = mock(SolicitacaoOrientacaoRepository.class);
        SolicitacaoOrientacao sol = new SolicitacaoOrientacao();
        sol.setStartupId(UUID.randomUUID());
        sol.setStatus(StatusSolicitacao.PENDENTE);
        when(repo.findByStartupId(sol.getStartupId())).thenReturn(List.of(sol));
        StartupService startupService = mock(StartupService.class);
        MentorService mentorService = mock(MentorService.class);
        VinculoService vinculoService = mock(VinculoService.class);
        SolicitacaoService s = new SolicitacaoService(new FixedObjectProvider<>(repo), startupService, mentorService, vinculoService);
        assertFalse(s.listarPorStartup(sol.getStartupId()).isEmpty());
    }

}
