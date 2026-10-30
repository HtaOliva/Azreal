package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.HistoricoResponseDTO;
import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.service.AceiteTermoService;
import br.senac.fullstack.service.AnotacaoService;
import br.senac.fullstack.service.HistoricoService;
import br.senac.fullstack.service.MentorService;
import br.senac.fullstack.service.NullObjectProvider;
import br.senac.fullstack.service.RecomendacaoService;
import br.senac.fullstack.service.SolicitacaoService;
import br.senac.fullstack.service.StartupService;
import br.senac.fullstack.service.VinculoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoricoControllerTest {

    private record Contexto(HistoricoController controller, StartupResponseDTO startup) { }

    private Contexto criarContexto() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        var aceite = aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1");
        StartupService startupService = new StartupService(new NullObjectProvider<>(), aceiteService);
        MentorService mentorService = new MentorService(new NullObjectProvider<>(), aceiteService);
        VinculoService vinculoService = new VinculoService(new NullObjectProvider<>(), mentorService);
        SolicitacaoService solicitacaoService = new SolicitacaoService(
                new NullObjectProvider<>(), startupService, mentorService, vinculoService);
        RecomendacaoService recomendacaoService = new RecomendacaoService(new NullObjectProvider<>(), vinculoService);
        AnotacaoService anotacaoService = new AnotacaoService(new NullObjectProvider<>(), vinculoService);
        HistoricoService historicoService = new HistoricoService(
                solicitacaoService, recomendacaoService, anotacaoService,
                startupService, mentorService, vinculoService);

        StartupResponseDTO startup = startupService.criar(new StartupRequestDTO(
                "Startup X", "startup@senac.br", "senha123", "Desc", "Tech",
                LocalDate.now(), Estagio.IDEACAO, List.of("Ana"), "SP", "BR", false, null, aceite.id()));

        return new Contexto(new HistoricoController(historicoService), startup);
    }

    @Test
    void buscarComAdminRetornaOk() {
        Contexto ctx = criarContexto();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        ResponseEntity<HistoricoResponseDTO> response = ctx.controller().buscar(UUID.randomUUID(), auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void buscarComStartupProprietariaRetornaOk() {
        Contexto ctx = criarContexto();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "startup@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));

        ResponseEntity<HistoricoResponseDTO> response = ctx.controller().buscar(ctx.startup().id(), auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
