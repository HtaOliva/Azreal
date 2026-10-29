package br.senac.fullstack.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HistoricoServiceTest {

    private MentorService mentorService() {
        return new MentorService(new NullObjectProvider<>(), new AceiteTermoService(new NullObjectProvider<>()));
    }

    private StartupService startupService() {
        return new StartupService(new NullObjectProvider<>(), new AceiteTermoService(new NullObjectProvider<>()));
    }

    private VinculoService vinculoService() {
        return new VinculoService(new NullObjectProvider<>(), mentorService());
    }

    private RecomendacaoService recomendacaoService() {
        return new RecomendacaoService(new NullObjectProvider<>(), vinculoService());
    }

    private AnotacaoService anotacaoService() {
        return new AnotacaoService(new NullObjectProvider<>(), vinculoService());
    }

    private SolicitacaoService solicitacaoService() {
        return new SolicitacaoService(new NullObjectProvider<>(), startupService(), mentorService(), vinculoService());
    }

    private HistoricoService service() {
        return new HistoricoService(solicitacaoService(), recomendacaoService(), anotacaoService(), startupService(), mentorService(), vinculoService());
    }

    @Test
    void verificarAcessoAdminNaoLanca() {
        HistoricoService s = service();
        assertDoesNotThrow(() -> s.verificarAcesso(UUID.randomUUID(), "user", true));
    }

    @Test
    void verificarAcessoNaoAdminLanca403() {
        HistoricoService s = service();
        assertThrows(Exception.class, () -> s.verificarAcesso(UUID.randomUUID(), "user", false));
    }

    @Test
    void buscarRetornaHistorico() {
        HistoricoService s = service();
        var h = s.buscar(UUID.randomUUID());
        assertNotNull(h);
    }
}
