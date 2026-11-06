package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.service.AmbienteValidacaoService;
import br.senac.fullstack.service.StartupService;
import br.senac.fullstack.service.TermoPilotoService;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TermoPilotoController.class)
class TermoPilotoControllerTest {

    private static final String STARTUP_EMAIL = "startup@azreal.com";
    private static final String OUTRA_EMAIL = "outra@azreal.com";
    private static final String AMBIENTE_EMAIL = "ambiente@senac.br";
    private static final String OUTRO_AMBIENTE_EMAIL = "outro@senac.br";
    private static final String ADMIN_EMAIL = "admin@azreal.com";
    private static final String URI_TERMOS = "/termos-piloto/{propostaId}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TermoPilotoService termoPilotoService;

    @MockBean
    private AmbienteValidacaoService ambienteValidacaoService;

    @MockBean
    private StartupService startupService;

    private TermoPiloto criarTermo(UUID propostaId, UUID startupId, UUID ambienteId) {
        TermoPiloto termo = new TermoPiloto();
        termo.setId(UUID.randomUUID());
        termo.setPropostaPocId(propostaId);
        termo.setStartupId(startupId);
        termo.setDesafioValidacaoId(UUID.randomUUID());
        termo.setAmbienteValidacaoId(ambienteId);
        termo.setEscopoTeste("Escopo congelado");
        termo.setDuracaoDias(30);
        termo.setResponsabilidadesStartup("Entrega APK");
        termo.setResponsabilidadesAmbiente("Disponibiliza sensores");
        termo.setMetricaValidacao("Acuracia > 90%");
        termo.setDataCriacao(LocalDateTime.now());
        return termo;
    }

    private AmbienteValidacaoResponseDTO criarAmbienteDTO(UUID id, String email) {
        return new AmbienteValidacaoResponseDTO(
                id,
                "Hospital Santa Joana",
                "Hospital Santa Joana PE",
                "Saude",
                PorteOrganizacao.GRANDE,
                "PE",
                "Recife",
                email,
                "81988887777",
                null,
                true,
                LocalDateTime.now()
        );
    }

    private StartupResponseDTO criarStartupDTO(UUID id, String email) {
        return new StartupResponseDTO(
                id,
                "BioTech Labs",
                email,
                "Descricao",
                "Biotecnologia",
                LocalDate.now().minusYears(1),
                Estagio.MVP_FUNCIONAL,
                List.of("Alice"),
                "Recife",
                "Recife",
                false,
                null,
                LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void startupProprietariaPodeConsultarTermoPorProposta() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPiloto termo = criarTermo(propostaId, startupId, ambienteId);
        when(termoPilotoService.buscarPorPropostaId(propostaId)).thenReturn(termo);
        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));
        when(ambienteValidacaoService.buscar(ambienteId)).thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propostaPocId").value(propostaId.toString()))
                .andExpect(jsonPath("$.startupId").value(startupId.toString()))
                .andExpect(jsonPath("$.razaoSocial").value("Hospital Santa Joana"))
                .andExpect(jsonPath("$.emailContato").value(AMBIENTE_EMAIL));
    }

    @Test
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void ambienteProprietarioPodeConsultarTermoPorProposta() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPiloto termo = criarTermo(propostaId, startupId, ambienteId);
        when(termoPilotoService.buscarPorPropostaId(propostaId)).thenReturn(termo);
        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL)).thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));
        when(ambienteValidacaoService.buscar(ambienteId)).thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambienteValidacaoId").value(ambienteId.toString()));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = {"ADMIN"})
    void adminPodeConsultarQualquerTermo() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPiloto termo = criarTermo(propostaId, UUID.randomUUID(), ambienteId);
        when(termoPilotoService.buscarPorPropostaId(propostaId)).thenReturn(termo);
        when(ambienteValidacaoService.buscar(ambienteId)).thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propostaPocId").value(propostaId.toString()));
    }

    @Test
    @WithMockUser(username = OUTRA_EMAIL, roles = {"STARTUP"})
    void outraStartupNaoPodeConsultarTermoRetornaForbidden() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID outraId = UUID.randomUUID();

        TermoPiloto termo = criarTermo(propostaId, startupId, UUID.randomUUID());
        when(termoPilotoService.buscarPorPropostaId(propostaId)).thenReturn(termo);
        when(startupService.buscarPorEmail(OUTRA_EMAIL)).thenReturn(criarStartupDTO(outraId, OUTRA_EMAIL));

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = OUTRO_AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void outroAmbienteNaoPodeConsultarTermoRetornaForbidden() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        UUID outroId = UUID.randomUUID();

        TermoPiloto termo = criarTermo(propostaId, UUID.randomUUID(), ambienteId);
        when(termoPilotoService.buscarPorPropostaId(propostaId)).thenReturn(termo);
        when(ambienteValidacaoService.buscarPorEmail(OUTRO_AMBIENTE_EMAIL))
                .thenReturn(criarAmbienteDTO(outroId, OUTRO_AMBIENTE_EMAIL));

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = {"ADMIN"})
    void termoNaoEncontradoRetornaNotFound() throws Exception {
        UUID propostaId = UUID.randomUUID();
        when(termoPilotoService.buscarPorPropostaId(propostaId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Termo nao encontrado"));

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void anonimoNaoPodeConsultarTermoRetornaUnauthorized() throws Exception {
        UUID propostaId = UUID.randomUUID();
        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void controllerDiretoComProvidersNulos() {
        TermoPilotoController controller = new TermoPilotoController(termoPilotoService);
        UUID propostaId = UUID.randomUUID();
        TermoPiloto termo = criarTermo(propostaId, UUID.randomUUID(), UUID.randomUUID());
        when(termoPilotoService.buscarPorPropostaId(propostaId)).thenReturn(termo);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                ADMIN_EMAIL, "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        ResponseEntity<TermoPilotoResponseDTO> resp = controller.buscarPorProposta(propostaId, auth);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }
}
