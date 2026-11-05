package br.senac.fullstack.controller;

import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.service.PropostaPocService;
import br.senac.fullstack.service.StartupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PropostaPocController.class)
class PropostaPocControllerTest {

    private static final String STARTUP_EMAIL = "startup@azreal.com";
    private static final String OUTRA_EMAIL = "outra@azreal.com";
    private static final String ADMIN_EMAIL = "admin@azreal.com";
    private static final String ESCOPO_TESTE = "Escopo do teste de telemetria";
    private static final String RESP_STARTUP = "Resp startup";
    private static final String RESP_AMBIENTE = "Resp ambiente";
    private static final String VALIDACAO_ACURACIA = "Validacao da acuracia";
    private static final String STATUS_PENDENTE = "PENDENTE";
    private static final String STATUS_CANCELADA = "CANCELADA";
    private static final String TAXA_SUPERIOR_90 = "Taxa > 90%";
    private static final String URI_PROPOSTAS = "/propostas-poc";
    private static final String URI_MINHAS = "/propostas-poc/minhas";
    private static final String URI_CANCELAR = "/propostas-poc/{id}/cancelar";
    private static final String URI_ID = "/propostas-poc/{id}";
    private static final String PARAM_STARTUP_ID = "startupId";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PropostaPocService propostaPocService;

    @MockBean
    private StartupService startupService;

    private StartupResponseDTO criarStartupDTO(UUID id, String email) {
        return new StartupResponseDTO(
                id,
                "Inova Med",
                email,
                "Descricao",
                "Saude",
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

    private PropostaPocResponseDTO criarPropostaResponseDTO(UUID id, UUID startupId, UUID desafioId, StatusProposta status) {
        return new PropostaPocResponseDTO(
                id,
                startupId,
                desafioId,
                ESCOPO_TESTE,
                30,
                RESP_STARTUP,
                RESP_AMBIENTE,
                "Disponibilidade > 99%",
                true,
                status,
                LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void startupAutenticadaSubmetePropostaComSucesso() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));

        PropostaPocRequestDTO request = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                VALIDACAO_ACURACIA,
                15,
                RESP_STARTUP,
                RESP_AMBIENTE,
                TAXA_SUPERIOR_90,
                true
        );

        when(propostaPocService.criar(any())).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));

        mockMvc.perform(post(URI_PROPOSTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(propostaId.toString()))
                .andExpect(jsonPath("$.status").value(STATUS_PENDENTE))
                .andExpect(jsonPath("$.escopoTeste").value(ESCOPO_TESTE));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    void adminPodeSubmeterPropostaParaQualquerStartup() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        PropostaPocRequestDTO request = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                VALIDACAO_ACURACIA,
                15,
                RESP_STARTUP,
                RESP_AMBIENTE,
                TAXA_SUPERIOR_90,
                true
        );

        when(propostaPocService.criar(any())).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));

        mockMvc.perform(post(URI_PROPOSTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(propostaId.toString()));
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void startupNaoPodeSubmeterPropostaEmNomeDeOutraStartup() throws Exception {
        UUID minhaStartupId = UUID.randomUUID();
        UUID outraStartupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(minhaStartupId, STARTUP_EMAIL));

        PropostaPocRequestDTO request = new PropostaPocRequestDTO(
                outraStartupId,
                desafioId,
                VALIDACAO_ACURACIA,
                15,
                RESP_STARTUP,
                RESP_AMBIENTE,
                TAXA_SUPERIOR_90,
                true
        );

        mockMvc.perform(post(URI_PROPOSTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO")
    void ambienteValidacaoRecebe403AoTentarSubmeterProposta() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocRequestDTO request = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                "Validacao indevida",
                15,
                RESP_STARTUP,
                RESP_AMBIENTE,
                TAXA_SUPERIOR_90,
                true
        );

        mockMvc.perform(post(URI_PROPOSTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void submissaoSemAceitePrevioRetornaBadRequest400() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocRequestDTO requestInvalido = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                "Escopo",
                15,
                RESP_STARTUP,
                RESP_AMBIENTE,
                "Metrica",
                false
        );

        mockMvc.perform(post(URI_PROPOSTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void startupCancelaPropostaPendenteComSucesso() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));
        when(propostaPocService.buscar(propostaId)).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));
        when(propostaPocService.cancelar(propostaId)).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.CANCELADA));

        mockMvc.perform(post(URI_CANCELAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propostaId.toString()))
                .andExpect(jsonPath("$.status").value(STATUS_CANCELADA));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    void adminPodeCancelarPropostaDeQualquerStartup() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(propostaPocService.cancelar(propostaId)).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.CANCELADA));

        mockMvc.perform(post(URI_CANCELAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(STATUS_CANCELADA));
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = OUTRA_EMAIL)
    void startupNaoPodeCancelarPropostaDeOutraStartup() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID outraStartupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail(OUTRA_EMAIL)).thenReturn(criarStartupDTO(outraStartupId, OUTRA_EMAIL));
        when(propostaPocService.buscar(propostaId)).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));

        mockMvc.perform(post(URI_CANCELAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void cancelarPropostaNaoPendenteRetorna400BadRequest() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));
        when(propostaPocService.buscar(propostaId)).thenReturn(criarPropostaResponseDTO(propostaId, startupId, UUID.randomUUID(), StatusProposta.EM_TESTE));
        when(propostaPocService.cancelar(propostaId)).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas propostas pendentes podem ser canceladas"));

        mockMvc.perform(post(URI_CANCELAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void listarMinhasPropostasRetornaPropostasDaStartupAutenticada() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));
        when(propostaPocService.listarPorStartup(startupId)).thenReturn(List.of(
                criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE)
        ));

        mockMvc.perform(get(URI_MINHAS)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(propostaId.toString()))
                .andExpect(jsonPath("$[0].status").value(STATUS_PENDENTE))
                .andExpect(jsonPath("$[0].startupId").value(startupId.toString()));
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void listarMinhasComParametroProprioRetornaOk() throws Exception {
        UUID startupId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));
        when(propostaPocService.listarPorStartup(startupId)).thenReturn(List.of());

        mockMvc.perform(get(URI_MINHAS)
                        .param(PARAM_STARTUP_ID, startupId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void listarMinhasSemStartupCadastradaRetornaListaVazia() throws Exception {
        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get(URI_MINHAS)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    void adminPodeListarMinhasComParametroQualquer() throws Exception {
        UUID startupId = UUID.randomUUID();
        when(propostaPocService.listarPorStartup(startupId)).thenReturn(List.of());

        mockMvc.perform(get(URI_MINHAS)
                        .param(PARAM_STARTUP_ID, startupId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void listarMinhasSemAutenticacaoRetorna401Unauthorized() throws Exception {
        mockMvc.perform(get(URI_MINHAS)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO")
    void listarMinhasComPerfilInvalidoRetorna403Forbidden() throws Exception {
        mockMvc.perform(get(URI_MINHAS)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarPropostaPorIdRetorna200Ok() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        when(propostaPocService.buscar(propostaId))
                .thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));

        mockMvc.perform(get(URI_ID, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propostaId.toString()))
                .andExpect(jsonPath("$.status").value(STATUS_PENDENTE));
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void submissaoAtingindoLimiteMaximoRetorna400BadRequest() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));

        PropostaPocRequestDTO request = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                VALIDACAO_ACURACIA,
                15,
                RESP_STARTUP,
                RESP_AMBIENTE,
                TAXA_SUPERIOR_90,
                true
        );

        when(propostaPocService.criar(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Startup atingiu o limite maximo de 3 propostas pendentes simultaneas"));

        mockMvc.perform(post(URI_PROPOSTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void submissaoDuplicadaParaMesmoDesafioRetorna400BadRequest() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));

        PropostaPocRequestDTO request = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                VALIDACAO_ACURACIA,
                15,
                RESP_STARTUP,
                RESP_AMBIENTE,
                TAXA_SUPERIOR_90,
                true
        );

        when(propostaPocService.criar(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Startup ja possui proposta ativa para este desafio de validacao"));

        mockMvc.perform(post(URI_PROPOSTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
