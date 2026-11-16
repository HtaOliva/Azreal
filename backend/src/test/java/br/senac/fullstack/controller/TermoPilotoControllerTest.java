package br.senac.fullstack.controller;

import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.service.TermoPilotoService;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TermoPilotoController.class)
class TermoPilotoControllerTest {

    private static final String STARTUP_EMAIL = "startup@azreal.com";
    private static final String OUTRA_EMAIL = "outra@azreal.com";
    private static final String AMBIENTE_EMAIL = "ambiente@senac.br";
    private static final String ADMIN_EMAIL = "admin@azreal.com";
    private static final String URI_TERMOS = "/termos-piloto/{propostaId}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TermoPilotoService termoPilotoService;

    private TermoPilotoResponseDTO criarDTO(UUID propostaId, UUID startupId, UUID ambienteId) {
        return new TermoPilotoResponseDTO(
                UUID.randomUUID(),
                propostaId,
                startupId,
                UUID.randomUUID(),
                ambienteId,
                "Escopo do piloto de IA",
                30,
                "Resp startup",
                "Resp ambiente",
                "Acuracia > 90%",
                LocalDateTime.now(),
                StatusProposta.EM_TESTE,
                "Hospital Santa Joana",
                "Hospital Santa Joana PE",
                AMBIENTE_EMAIL,
                "81988887777",
                "Saude",
                PorteOrganizacao.GRANDE
        );
    }

    @Test
    @DisplayName("Startup proprietaria pode consultar termo via GET /termos-piloto/{propostaId}")
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void startupProprietariaPodeConsultarTermoPorProposta() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPilotoResponseDTO dto = criarDTO(propostaId, startupId, ambienteId);
        when(termoPilotoService.obterTermoComIdentidadeRevelada(eq(propostaId), any(Authentication.class)))
                .thenReturn(dto);

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propostaPocId").value(propostaId.toString()))
                .andExpect(jsonPath("$.startupId").value(startupId.toString()))
                .andExpect(jsonPath("$.razaoSocial").value("Hospital Santa Joana"))
                .andExpect(jsonPath("$.emailContato").value(AMBIENTE_EMAIL));
    }

    @Test
    @DisplayName("Ambiente proprietario pode consultar termo por proposta")
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void ambienteProprietarioPodeConsultarTermoPorProposta() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPilotoResponseDTO dto = criarDTO(propostaId, startupId, ambienteId);
        when(termoPilotoService.obterTermoComIdentidadeRevelada(eq(propostaId), any(Authentication.class)))
                .thenReturn(dto);

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambienteValidacaoId").value(ambienteId.toString()));
    }

    @Test
    @DisplayName("Admin pode consultar qualquer termo por proposta")
    @WithMockUser(username = ADMIN_EMAIL, roles = {"ADMIN"})
    void adminPodeConsultarQualquerTermo() throws Exception {
        UUID propostaId = UUID.randomUUID();
        TermoPilotoResponseDTO dto = criarDTO(propostaId, UUID.randomUUID(), UUID.randomUUID());
        when(termoPilotoService.obterTermoComIdentidadeRevelada(eq(propostaId), any(Authentication.class)))
                .thenReturn(dto);

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propostaPocId").value(propostaId.toString()));
    }

    @Test
    @DisplayName("Usuario sem permissao recebe 403 Forbidden ao consultar termo")
    @WithMockUser(username = OUTRA_EMAIL, roles = {"STARTUP"})
    void usuarioNaoAutorizadoRecebeForbiddenAoConsultarTermo() throws Exception {
        UUID propostaId = UUID.randomUUID();
        when(termoPilotoService.obterTermoComIdentidadeRevelada(eq(propostaId), any(Authentication.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Consulta de termo inexistente retorna 404 Not Found")
    @WithMockUser(username = ADMIN_EMAIL, roles = {"ADMIN"})
    void termoNaoEncontradoRetornaNotFound() throws Exception {
        UUID propostaId = UUID.randomUUID();
        when(termoPilotoService.obterTermoComIdentidadeRevelada(eq(propostaId), any(Authentication.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Termo nao encontrado"));

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Usuario anonimo recebe 401 Unauthorized")
    void anonimoNaoPodeConsultarTermoRetornaUnauthorized() throws Exception {
        UUID propostaId = UUID.randomUUID();
        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invocacao direta do controller delega para o servico")
    void controllerDiretoInvocaService() {
        TermoPilotoController controller = new TermoPilotoController(termoPilotoService);
        UUID propostaId = UUID.randomUUID();
        TermoPilotoResponseDTO dto = criarDTO(propostaId, UUID.randomUUID(), UUID.randomUUID());
        when(termoPilotoService.obterTermoComIdentidadeRevelada(eq(propostaId), any(Authentication.class)))
                .thenReturn(dto);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                ADMIN_EMAIL, "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        ResponseEntity<TermoPilotoResponseDTO> resp = controller.buscarPorProposta(propostaId, auth);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }
}
