package br.senac.fullstack.controller;

import br.senac.fullstack.dto.ConfirmacaoExecucaoRequestDTO;
import br.senac.fullstack.dto.ConfirmacaoExecucaoResponseDTO;
import br.senac.fullstack.dto.EntregaPilotoRequestDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TermoPilotoController.class)
class TermoPilotoControllerTest {

    private static final String STARTUP_EMAIL = "startup@azreal.com";
    private static final String OUTRA_EMAIL = "outra@azreal.com";
    private static final String AMBIENTE_EMAIL = "ambiente@senac.br";
    private static final String ADMIN_EMAIL = "admin@azreal.com";
    private static final String URI_TERMOS = "/termos-piloto/{propostaId}";
    private static final String URI_REGISTRAR_ENTREGA = "/termos-piloto/{id}/registrar-entrega";
    private static final String URI_CONFIRMAR_EXECUCAO = "/termos-piloto/{id}/confirmar-execucao";

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
                .andExpect(jsonPath("$.propostaPocId").value(propostaId.toString()));
    }

    @Test
    @DisplayName("Admin pode consultar qualquer termo")
    @WithMockUser(username = ADMIN_EMAIL, roles = {"ADMIN"})
    void adminPodeConsultarQualquerTermo() throws Exception {
        UUID propostaId = UUID.randomUUID();
        TermoPilotoResponseDTO dto = criarDTO(propostaId, UUID.randomUUID(), UUID.randomUUID());
        when(termoPilotoService.obterTermoComIdentidadeRevelada(eq(propostaId), any(Authentication.class)))
                .thenReturn(dto);

        mockMvc.perform(get(URI_TERMOS, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Usuario nao autorizado recebe 403 Forbidden")
    @WithMockUser(username = OUTRA_EMAIL, roles = {"STARTUP"})
    void usuarioNaoAutorizadoRecebeForbidden() throws Exception {
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
    @DisplayName("RF18 - Startup pode registrar entrega com sucesso")
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void startupPodeRegistrarEntregaComSucesso() throws Exception {
        UUID termoId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setPropostaPocId(UUID.randomUUID());
        termo.setStartupId(UUID.randomUUID());
        termo.setAmbienteValidacaoId(UUID.randomUUID());
        termo.setStatus(StatusProposta.EM_TESTE);
        termo.registrarEntrega("Entrega de modelo com 93% de acuracia", "https://link.com", "Sem ressalvas");

        when(termoPilotoService.registrarEntrega(eq(termoId), any(EntregaPilotoRequestDTO.class), any(Authentication.class)))
                .thenReturn(termo);

        String json = """
                {
                    "resumoEntrega": "Entrega de modelo com 93% de acuracia",
                    "linksEvidencias": "https://link.com",
                    "observacoes": "Sem ressalvas"
                }
                """;

        mockMvc.perform(post(URI_REGISTRAR_ENTREGA, termoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(termoId.toString()))
                .andExpect(jsonPath("$.resumoEntrega").value("Entrega de modelo com 93% de acuracia"))
                .andExpect(jsonPath("$.statusProposta").value("EM_TESTE"));
    }

    @Test
    @DisplayName("RF18 - Registrar entrega sem resumo deve retornar 400 Bad Request")
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void registrarEntregaSemResumoRetornaBadRequest() throws Exception {
        UUID termoId = UUID.randomUUID();
        String json = """
                {
                    "resumoEntrega": ""
                }
                """;

        mockMvc.perform(post(URI_REGISTRAR_ENTREGA, termoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RF18 - Registrar entrega por perfil nao autorizado (AMBIENTE_VALIDACAO) retorna 403")
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void registrarEntregaPorAmbienteRetornaForbidden() throws Exception {
        UUID termoId = UUID.randomUUID();
        String json = """
                {
                    "resumoEntrega": "Resumo de teste"
                }
                """;

        mockMvc.perform(post(URI_REGISTRAR_ENTREGA, termoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RF19 - Parceiro pode confirmar execucao do piloto com avaliacao de metrica e modo de privacidade")
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void parceiroPodeConfirmarExecucaoComSucesso() throws Exception {
        UUID termoId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setPropostaPocId(UUID.randomUUID());
        termo.setStartupId(UUID.randomUUID());
        termo.setAmbienteValidacaoId(UUID.randomUUID());
        termo.setStatus(StatusProposta.EM_TESTE);

        termo.confirmarExecucao(
                true,
                AvaliacaoMetrica.ATENDEU_TOTALMENTE,
                "Atingiu o objetivo perfeitamente",
                ModoPrivacidadePoc.EXIBICAO_NOMINAL,
                "Contratacao aprovada",
                null
        );

        when(termoPilotoService.confirmarExecucao(eq(termoId), any(ConfirmacaoExecucaoRequestDTO.class), any(Authentication.class)))
                .thenReturn(termo);

        String json = """
                {
                    "pilotoExecutado": true,
                    "avaliacaoMetrica": "ATENDEU_TOTALMENTE",
                    "parecer": "Atingiu o objetivo perfeitamente",
                    "modoPrivacidade": "EXIBICAO_NOMINAL",
                    "direcionamentoPosPoc": "Contratacao aprovada"
                }
                """;

        mockMvc.perform(post(URI_CONFIRMAR_EXECUCAO, termoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POC_CONCLUIDA"))
                .andExpect(jsonPath("$.pilotoExecutado").value(true))
                .andExpect(jsonPath("$.avaliacaoMetrica").value("ATENDEU_TOTALMENTE"))
                .andExpect(jsonPath("$.parecer").value("Atingiu o objetivo perfeitamente"))
                .andExpect(jsonPath("$.modoPrivacidade").value("EXIBICAO_NOMINAL"))
                .andExpect(jsonPath("$.certificadoEmitido").value(false));
    }

    @Test
    @DisplayName("RF19 - Parceiro confirma que teste nao aconteceu (pilotoExecutado=false) -> POC_NAO_CONFIRMADA sem certificado")
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void parceiroConfirmaQuePilotoNaoAconteceu() throws Exception {
        UUID termoId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setPropostaPocId(UUID.randomUUID());
        termo.setStartupId(UUID.randomUUID());
        termo.setAmbienteValidacaoId(UUID.randomUUID());
        termo.setStatus(StatusProposta.EM_TESTE);

        termo.confirmarExecucao(
                false,
                null,
                null,
                null,
                null,
                "Teste nao aconteceu"
        );

        when(termoPilotoService.confirmarExecucao(eq(termoId), any(ConfirmacaoExecucaoRequestDTO.class), any(Authentication.class)))
                .thenReturn(termo);

        String json = """
                {
                    "pilotoExecutado": false,
                    "justificativaNaoExecucao": "Teste nao aconteceu"
                }
                """;

        mockMvc.perform(post(URI_CONFIRMAR_EXECUCAO, termoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POC_NAO_CONFIRMADA"))
                .andExpect(jsonPath("$.pilotoExecutado").value(false))
                .andExpect(jsonPath("$.certificadoEmitido").value(false));
    }

    @Test
    @DisplayName("RF19 - Startup nao pode confirmar execucao (deve retornar 403 Forbidden)")
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void startupNaoPodeConfirmarExecucaoRetornaForbidden() throws Exception {
        UUID termoId = UUID.randomUUID();
        String json = """
                {
                    "pilotoExecutado": true,
                    "avaliacaoMetrica": "ATENDEU_TOTALMENTE",
                    "parecer": "Parecer",
                    "modoPrivacidade": "EXIBICAO_NOMINAL"
                }
                """;

        mockMvc.perform(post(URI_CONFIRMAR_EXECUCAO, termoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
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

        UUID termoId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setStatus(StatusProposta.EM_TESTE);

        EntregaPilotoRequestDTO entregaReq = new EntregaPilotoRequestDTO("Resumo", null, null);
        when(termoPilotoService.registrarEntrega(eq(termoId), eq(entregaReq), any(Authentication.class)))
                .thenReturn(termo);

        ResponseEntity<TermoPilotoResponseDTO> entregaResp = controller.registrarEntrega(termoId, entregaReq, auth);
        assertEquals(HttpStatus.OK, entregaResp.getStatusCode());

        ConfirmacaoExecucaoRequestDTO confReq = new ConfirmacaoExecucaoRequestDTO(
                false, null, null, null, null, "Motivo");
        termo.confirmarExecucao(false, null, null, null, null, "Motivo");
        when(termoPilotoService.confirmarExecucao(eq(termoId), eq(confReq), any(Authentication.class)))
                .thenReturn(termo);

        ResponseEntity<ConfirmacaoExecucaoResponseDTO> confResp = controller.confirmarExecucao(termoId, confReq, auth);
        assertEquals(HttpStatus.OK, confResp.getStatusCode());
    }
}
