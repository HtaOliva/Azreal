package br.senac.fullstack.service;

import br.senac.fullstack.dto.FilaItemResponseDTO;
import br.senac.fullstack.dto.RespostaPedidoRequestDTO;
import br.senac.fullstack.dto.SolicitacaoRequestDTO;
import br.senac.fullstack.dto.SolicitacaoResponseDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.model.SolicitacaoOrientacao;
import br.senac.fullstack.model.StatusSolicitacao;
import br.senac.fullstack.repository.SolicitacaoOrientacaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
        SolicitacaoService s = new SolicitacaoService(
                new FixedObjectProvider<>(repo), startupService, mentorService, vinculoService);
        when(startupService.buscar(any())).thenReturn(null);
        when(mentorService.buscar(any())).thenReturn(null);
        SolicitacaoRequestDTO req = new SolicitacaoRequestDTO(sol.getStartupId(), sol.getMentorId(), "assunto");
        SolicitacaoResponseDTO r = s.criar(req);
        assertNotNull(r);
    }

    @Test
    void criarComDoisPedidosAbertosLanca422() {
        SolicitacaoOrientacaoRepository repo = mock(SolicitacaoOrientacaoRepository.class);
        UUID startupId = UUID.randomUUID();
        SolicitacaoOrientacao s1 = new SolicitacaoOrientacao();
        s1.setStartupId(startupId);
        s1.setStatus(StatusSolicitacao.PENDENTE);
        SolicitacaoOrientacao s2 = new SolicitacaoOrientacao();
        s2.setStartupId(startupId);
        s2.setStatus(StatusSolicitacao.PENDENTE);
        SolicitacaoOrientacao s3 = new SolicitacaoOrientacao();
        s3.setStartupId(startupId);
        s3.setStatus(StatusSolicitacao.RECUSADO);

        when(repo.findByStartupId(startupId)).thenReturn(List.of(s1, s2, s3));
        StartupService startupService = mock(StartupService.class);
        MentorService mentorService = mock(MentorService.class);
        VinculoService vinculoService = mock(VinculoService.class);
        SolicitacaoService s = new SolicitacaoService(
                new FixedObjectProvider<>(repo), startupService, mentorService, vinculoService);

        assertThrows(ResponseStatusException.class, () ->
                s.criar(new SolicitacaoRequestDTO(startupId, UUID.randomUUID(), "assunto")));
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
        SolicitacaoService s = new SolicitacaoService(
                new FixedObjectProvider<>(repo), startupService, mentorService, vinculoService);
        assertFalse(s.listarPorStartup(sol.getStartupId()).isEmpty());
    }

    @Test
    void responderCenariosCompletosComRepo() {
        SolicitacaoOrientacaoRepository repo = mock(SolicitacaoOrientacaoRepository.class);
        StartupService startupService = mock(StartupService.class);
        MentorService mentorService = mock(MentorService.class);
        VinculoService vinculoService = mock(VinculoService.class);
        SolicitacaoService s = new SolicitacaoService(
                new FixedObjectProvider<>(repo), startupService, mentorService, vinculoService);

        UUID mentorId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();

        // 1. Não encontrado -> 404
        UUID idInexistente = UUID.randomUUID();
        when(repo.findById(idInexistente)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () ->
                s.responder(idInexistente, mentorId, new RespostaPedidoRequestDTO(true, "ok")));

        // 2. Outro mentor -> 403
        SolicitacaoOrientacao solOutro = new SolicitacaoOrientacao();
        solOutro.setMentorId(UUID.randomUUID());
        when(repo.findById(solOutro.getId())).thenReturn(Optional.of(solOutro));
        assertThrows(ResponseStatusException.class, () ->
                s.responder(solOutro.getId(), mentorId, new RespostaPedidoRequestDTO(true, "ok")));

        // 3. Status não pendente -> 409
        SolicitacaoOrientacao solRespondida = new SolicitacaoOrientacao();
        solRespondida.setMentorId(mentorId);
        solRespondida.setStatus(StatusSolicitacao.ACEITO);
        when(repo.findById(solRespondida.getId())).thenReturn(Optional.of(solRespondida));
        assertThrows(ResponseStatusException.class, () ->
                s.responder(solRespondida.getId(), mentorId, new RespostaPedidoRequestDTO(true, "ok")));

        // 4. Expirado -> 410
        SolicitacaoOrientacao solExpirada = new SolicitacaoOrientacao();
        solExpirada.setMentorId(mentorId);
        solExpirada.setStatus(StatusSolicitacao.PENDENTE);
        solExpirada.setDataExpiracao(LocalDateTime.now().minusDays(1));
        when(repo.findById(solExpirada.getId())).thenReturn(Optional.of(solExpirada));
        assertThrows(ResponseStatusException.class, () ->
                s.responder(solExpirada.getId(), mentorId, new RespostaPedidoRequestDTO(true, "ok")));
        assertEquals(StatusSolicitacao.EXPIRADO, solExpirada.getStatus());

        // 5. Recusar -> RECUSADO
        SolicitacaoOrientacao solRecusar = new SolicitacaoOrientacao();
        solRecusar.setMentorId(mentorId);
        solRecusar.setStartupId(startupId);
        solRecusar.setStatus(StatusSolicitacao.PENDENTE);
        solRecusar.setDataExpiracao(LocalDateTime.now().plusDays(5));
        when(repo.findById(solRecusar.getId())).thenReturn(Optional.of(solRecusar));
        SolicitacaoResponseDTO respRecusada = s.responder(
                solRecusar.getId(), mentorId, new RespostaPedidoRequestDTO(false, "Sem agenda"));
        assertEquals(StatusSolicitacao.RECUSADO, respRecusada.status());
    }

    @Test
    void filaDoMentorComRepoEFallbackDeStartup() {
        SolicitacaoOrientacaoRepository repo = mock(SolicitacaoOrientacaoRepository.class);
        StartupService startupService = mock(StartupService.class);
        MentorService mentorService = mock(MentorService.class);
        VinculoService vinculoService = mock(VinculoService.class);
        SolicitacaoService s = new SolicitacaoService(
                new FixedObjectProvider<>(repo), startupService, mentorService, vinculoService);

        UUID mentorId = UUID.randomUUID();
        SolicitacaoOrientacao s1 = new SolicitacaoOrientacao();
        s1.setStartupId(UUID.randomUUID());
        s1.setMentorId(mentorId);
        s1.setAssunto("Assunto 1");

        SolicitacaoOrientacao s2 = new SolicitacaoOrientacao();
        s2.setStartupId(UUID.randomUUID());
        s2.setMentorId(mentorId);
        s2.setAssunto("Assunto 2");

        when(repo.findByMentorIdAndStatus(mentorId, StatusSolicitacao.PENDENTE)).thenReturn(List.of(s1, s2));
        when(startupService.buscar(s1.getStartupId())).thenReturn(new StartupResponseDTO(
                s1.getStartupId(), "Startup S1", "s1@senac.br", null, null, null, null, null, null, null, false, null, null));
        when(startupService.buscar(s2.getStartupId())).thenThrow(new RuntimeException("Nao encontrada"));

        List<FilaItemResponseDTO> fila = s.filaDoMentor(mentorId);
        assertEquals(2, fila.size());
        assertEquals("Startup S1", fila.get(0).startupNome());
        assertEquals("Startup " + s2.getStartupId(), fila.get(1).startupNome());
    }
}
