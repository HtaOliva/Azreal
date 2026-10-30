package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.MentorRequestDTO;
import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.model.Estagio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HistoricoServiceTest {

    private record Contexto(
            HistoricoService historicoService,
            StartupService startupService,
            MentorService mentorService,
            VinculoService vinculoService,
            UUID aceiteId
    ) { }

    private Contexto criarContexto() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        UUID aceiteId = aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
        MentorService ms = new MentorService(new NullObjectProvider<>(), aceiteService);
        StartupService ss = new StartupService(new NullObjectProvider<>(), aceiteService);
        VinculoService vs = new VinculoService(new NullObjectProvider<>(), ms);
        RecomendacaoService rs = new RecomendacaoService(new NullObjectProvider<>(), vs);
        AnotacaoService as = new AnotacaoService(new NullObjectProvider<>(), vs);
        SolicitacaoService sols = new SolicitacaoService(new NullObjectProvider<>(), ss, ms, vs);
        HistoricoService hs = new HistoricoService(sols, rs, as, ss, ms, vs);
        return new Contexto(hs, ss, ms, vs, aceiteId);
    }

    @Test
    void verificarAcessoAdminNaoLanca() {
        Contexto ctx = criarContexto();
        assertDoesNotThrow(() -> ctx.historicoService().verificarAcesso(UUID.randomUUID(), "user", true));
    }

    @Test
    void verificarAcessoNaoAdminLanca403() {
        Contexto ctx = criarContexto();
        assertThrows(Exception.class, () -> ctx.historicoService().verificarAcesso(UUID.randomUUID(), "user", false));
    }

    @Test
    void verificarAcessoMentorVinculadoENaoVinculado() {
        Contexto ctx = criarContexto();
        var startup = ctx.startupService().criar(new StartupRequestDTO(
                "Startup H", "sh@senac.br", "senha123", "Desc", "Tech",
                LocalDate.now(), Estagio.IDEACAO, List.of("A"), "SP", "BR", false, null, ctx.aceiteId()));
        var mentor = ctx.mentorService().criar(new MentorRequestDTO(
                "Mentor H", "mh@senac.br", "senha123", List.of("Tech"), List.of("IA"),
                "5 anos", "Semanal", "SP", "11999", "link", ctx.aceiteId()));

        // Mentor existe mas ainda não tem vínculo ativo -> lança 403
        assertThrows(Exception.class, () ->
                ctx.historicoService().verificarAcesso(startup.id(), "mh@senac.br", false));

        // Após criar vínculo ativo -> acesso permitido
        ctx.vinculoService().criar(startup.id(), mentor.id());
        assertDoesNotThrow(() ->
                ctx.historicoService().verificarAcesso(startup.id(), "mh@senac.br", false));
    }

    @Test
    void buscarRetornaHistorico() {
        Contexto ctx = criarContexto();
        var h = ctx.historicoService().buscar(UUID.randomUUID());
        assertNotNull(h);
    }
}
