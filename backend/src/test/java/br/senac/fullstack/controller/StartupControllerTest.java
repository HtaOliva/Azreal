package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.RegistroOficialPocResponseDTO;
import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.StartupUpdateDTO;
import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.Startup;
import br.senac.fullstack.repository.StartupRepository;
import br.senac.fullstack.service.AceiteTermoService;
import br.senac.fullstack.service.FixedObjectProvider;
import br.senac.fullstack.service.NullObjectProvider;
import br.senac.fullstack.service.RegistroOficialPocService;
import br.senac.fullstack.service.StartupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StartupController.class)
class StartupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StartupService startupService;

    @MockBean
    private RegistroOficialPocService registroOficialPocService;

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO")
    void ambienteValidacaoRecebe403AoAtualizarStartup() throws Exception {
        UUID id = UUID.randomUUID();
        when(startupService.atualizar(any(), any())).thenReturn(null);

        mockMvc.perform(put("/startups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void semAutenticacaoRecebe401AoAtualizarStartup() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/startups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private record Contexto(StartupController controller, StartupResponseDTO startup) { }

    private Contexto criarContexto() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        var aceite = aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1");
        StartupService serviceReal = new StartupService(new NullObjectProvider<>(), aceiteService);
        StartupController controller = new StartupController(serviceReal);

        StartupRequestDTO req = new StartupRequestDTO(
                "Startup Nook", "nook@senac.br", "senha123", "Plataforma de validacao",
                "Healthtech", LocalDate.now(), Estagio.MVP_FUNCIONAL, List.of("Ana"),
                "Recife", "PE", true, "Incubadora Senac", aceite.id(),
                "PE", "Recife", "Validar triagem hospitalar em ambiente real",
                List.of(UUID.randomUUID()), List.of(ModalidadeTeste.PRESENCIAL, ModalidadeTeste.HIBRIDO));
        ResponseEntity<StartupResponseDTO> criada = controller.criar(req);
        return new Contexto(controller, criada.getBody());
    }

    @Test
    void criarBuscarEListarStartupFuncionam() {
        Contexto ctx = criarContexto();
        assertNotNull(ctx.startup());

        ResponseEntity<StartupResponseDTO> buscada = ctx.controller().buscar(ctx.startup().id());
        assertEquals(HttpStatus.OK, buscada.getStatusCode());
        assertEquals("Startup Nook", buscada.getBody().nome());
        assertEquals("PE", buscada.getBody().ufIbge());
        assertEquals("Recife", buscada.getBody().municipioIbge());

        ResponseEntity<List<StartupResponseDTO>> lista = ctx.controller().listar();
        assertEquals(HttpStatus.OK, lista.getStatusCode());
        assertFalse(lista.getBody().isEmpty());
    }

    @Test
    void atualizarComRoleAdminRetornaOk() {
        Contexto ctx = criarContexto();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        StartupUpdateDTO update = new StartupUpdateDTO(
                "Startup Nook Admin", "Nova Desc", "AI", Estagio.EM_TESTE,
                List.of("Ana", "Bia"), "Recife", "Nordeste", false, null,
                "PE", "Olinda", "Validar integracao", List.of(), List.of(ModalidadeTeste.REMOTO));

        ResponseEntity<StartupResponseDTO> resp = ctx.controller().atualizar(ctx.startup().id(), update, auth);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Startup Nook Admin", resp.getBody().nome());
        assertEquals("Olinda", resp.getBody().municipioIbge());
    }

    @Test
    void atualizarComMesmoEmailDaStartupRetornaOk() {
        Contexto ctx = criarContexto();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "nook@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));
        StartupUpdateDTO update = new StartupUpdateDTO(
                "Startup Nook Propria", null, null, null, null, null, null, null, null);

        ResponseEntity<StartupResponseDTO> resp = ctx.controller().atualizar(ctx.startup().id(), update, auth);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Startup Nook Propria", resp.getBody().nome());
    }

    @Test
    void atualizarComEmailDiferenteRetorna403() {
        Contexto ctx = criarContexto();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "outra@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));
        StartupUpdateDTO update = new StartupUpdateDTO(
                "Invasora", null, null, null, null, null, null, null, null);

        ResponseEntity<StartupResponseDTO> resp = ctx.controller().atualizar(ctx.startup().id(), update, auth);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void atualizarQuandoEmailDaStartupForNuloRetorna403() {
        StartupRepository repo = mock(StartupRepository.class);
        Startup startupSemEmail = new Startup();
        UUID id = startupSemEmail.getId();
        startupSemEmail.setNome("Sem Email");
        startupSemEmail.setEmail(null);
        when(repo.findById(id)).thenReturn(Optional.of(startupSemEmail));

        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        StartupService serviceReal = new StartupService(new FixedObjectProvider<>(repo), aceiteService);
        StartupController controller = new StartupController(serviceReal);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "qualquer@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));
        StartupUpdateDTO update = new StartupUpdateDTO(
                "Tentativa", null, null, null, null, null, null, null, null);

        ResponseEntity<StartupResponseDTO> resp = controller.atualizar(id, update, auth);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    @DisplayName("GET /startups/{id}/pocs retorna lista de registros oficiais de POC da startup")
    void deveListarPocsPortfolioStartup() throws Exception {
        UUID startupId = UUID.randomUUID();
        RegistroOficialPocResponseDTO dto = new RegistroOficialPocResponseDTO(
                UUID.randomUUID(),
                "POC-2026-PORT0001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                startupId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Escopo testado",
                30,
                AvaliacaoMetrica.ATENDEU_TOTALMENTE,
                "Parecer positivo",
                ModoPrivacidadePoc.EXIBICAO_NOMINAL,
                "Direcionamento",
                "Metrica KPI",
                "Resumo",
                "Parceiro Real S.A.",
                "Tecnologia",
                PorteOrganizacao.MEDIO,
                LocalDateTime.now(),
                false
        );

        when(registroOficialPocService.listarPorStartup(startupId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/startups/{id}/pocs", startupId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoVerificador").value("POC-2026-PORT0001"))
                .andExpect(jsonPath("$[0].escopoTestado").value("Escopo testado"))
                .andExpect(jsonPath("$[0].nomeOrganizacao").value("Parceiro Real S.A."));

        mockMvc.perform(get("/startups/{id}/registros-poc", startupId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoVerificador").value("POC-2026-PORT0001"));
    }

    @Test
    @DisplayName("Invocacao direta de listarPocsPortfolio trata provider ausente")
    void invocacaoDiretaListarPocsTrataProviderNulo() {
        StartupController controllerSemProvider = new StartupController(startupService);
        ResponseEntity<List<RegistroOficialPocResponseDTO>> resp =
                controllerSemProvider.listarPocsPortfolio(UUID.randomUUID());
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
    }
}
