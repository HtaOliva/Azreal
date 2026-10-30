package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.MentorRequestDTO;
import br.senac.fullstack.dto.RespostaPedidoRequestDTO;
import br.senac.fullstack.dto.SolicitacaoRequestDTO;
import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.model.Estagio;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitacaoServiceTest {

    private record Contexto(
            SolicitacaoService solicitacaoService,
            StartupService startupService,
            MentorService mentorService,
            UUID aceiteId
    ) { }

    private Contexto criarContexto() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        UUID aceiteId = aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
        MentorService ms = new MentorService(new NullObjectProvider<>(), aceiteService);
        StartupService ss = new StartupService(new NullObjectProvider<>(), aceiteService);
        VinculoService vs = new VinculoService(new NullObjectProvider<>(), ms);
        SolicitacaoService sols = new SolicitacaoService(new NullObjectProvider<>(), ss, ms, vs);
        return new Contexto(sols, ss, ms, aceiteId);
    }

    @Test
    void criarSemStartupExistenteLanca() {
        Contexto ctx = criarContexto();
        SolicitacaoService service = ctx.solicitacaoService();
        SolicitacaoRequestDTO req = new SolicitacaoRequestDTO(UUID.randomUUID(), UUID.randomUUID(), "assunto");
        assertThrows(Exception.class, () -> service.criar(req));
    }

    @Test
    void filaDoMentorSemRepoRetornaVazio() {
        Contexto ctx = criarContexto();
        assertTrue(ctx.solicitacaoService().filaDoMentor(UUID.randomUUID()).isEmpty());
    }

    @Test
    void listarPorStartupSemRepoRetornaVazio() {
        Contexto ctx = criarContexto();
        assertTrue(ctx.solicitacaoService().listarPorStartup(UUID.randomUUID()).isEmpty());
    }

    @Test
    void criarEmMemoriaLimiteDoisPedidosEListarPorStartup() {
        Contexto ctx = criarContexto();
        var s1 = ctx.startupService().criar(new StartupRequestDTO(
                "Startup 1", "s1@senac.br", "senha123", "Desc", "Tech",
                LocalDate.now(), Estagio.IDEACAO, List.of("A"), "SP", "BR", false, null, ctx.aceiteId()));
        var m1 = ctx.mentorService().criar(new MentorRequestDTO(
                "Mentor 1", "m1@senac.br", "senha123", List.of("Tech"), List.of("IA"),
                "5 anos", "Semanal", "SP", "11999", "link", ctx.aceiteId()));

        SolicitacaoService service = ctx.solicitacaoService();
        UUID startupId = s1.id();
        UUID mentorId = m1.id();

        service.criar(new SolicitacaoRequestDTO(startupId, mentorId, "Pedido 1"));
        service.criar(new SolicitacaoRequestDTO(startupId, mentorId, "Pedido 2"));

        SolicitacaoRequestDTO pedido3 = new SolicitacaoRequestDTO(startupId, mentorId, "Pedido 3");
        assertThrows(ResponseStatusException.class, () -> service.criar(pedido3));

        var lista = service.listarPorStartup(startupId);
        assertEquals(2, lista.size());
        assertFalse(service.filaDoMentor(mentorId).isEmpty());

        UUID idInexistente = UUID.randomUUID();
        RespostaPedidoRequestDTO respostaOk = new RespostaPedidoRequestDTO(true, "ok");
        assertThrows(ResponseStatusException.class, () ->
                service.responder(idInexistente, mentorId, respostaOk));
    }
}
