package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.ConfirmacaoExecucaoRequestDTO;
import br.senac.fullstack.dto.ConfirmacaoExecucaoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoResponseDTO;
import br.senac.fullstack.dto.EntregaPilotoRequestDTO;
import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.dto.RecusaPropostaRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.model.AmbienteValidacao;
import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusDesafio;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.service.AmbienteValidacaoService;
import br.senac.fullstack.service.DesafioValidacaoService;
import br.senac.fullstack.service.PropostaPocService;
import br.senac.fullstack.service.StartupService;
import br.senac.fullstack.service.TermoPilotoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.ObjectProvider;
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
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
    private static final String URI_FILA = "/propostas-poc/fila";
    private static final String AMBIENTE_EMAIL = "ambiente@senac.br";
    private static final String PARAM_STARTUP_ID = "startupId";
    private static final String PARAM_AMBIENTE_ID = "ambienteId";
    private static final String STATUS_RECUSADA = "RECUSADA";
    private static final String URI_RECUSAR = "/propostas-poc/{id}/recusar";
    private static final String URI_ACEITAR = "/propostas-poc/{id}/aceitar";
    private static final String URI_TERMO = "/propostas-poc/{id}/termo";
    private static final String MOTIVO_RECUSA = "Fora do escopo do desafio";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PropostaPocService propostaPocService;

    @MockBean
    private StartupService startupService;

    @MockBean
    private AmbienteValidacaoService ambienteValidacaoService;

    @MockBean
    private DesafioValidacaoService desafioValidacaoService;

    @MockBean
    private TermoPilotoService termoPilotoService;

    private AmbienteValidacaoResponseDTO criarAmbienteDTO(UUID id, String email) {
        return new AmbienteValidacaoResponseDTO(
                id,
                "Hospital Esperanca",
                "Hospital Esperanca PE",
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

    private DesafioValidacaoResponseDTO criarDesafioDTO(UUID id, UUID ambienteId) {
        DesafioValidacao desafio = new DesafioValidacao();
        desafio.setId(id);
        AmbienteValidacao amb = new AmbienteValidacao();
        amb.setId(ambienteId);
        desafio.setAmbienteValidacao(amb);
        desafio.setTitulo("Triagem IA");
        desafio.setStatus(StatusDesafio.ABERTO);
        desafio.setAtivo(true);
        desafio.setDataCriacao(LocalDateTime.now());
        return DesafioValidacaoResponseDTO.fromEntity(desafio);
    }

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

    private PropostaPocResponseDTO criarPropostaRecusadaResponseDTO(
            UUID id, UUID startupId, UUID desafioId, String motivoRecusa) {
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
                StatusProposta.RECUSADA,
                LocalDateTime.now(),
                null,
                motivoRecusa
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

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void startupCriarQuandoBuscarPorEmailFalhaProssegueParaService() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenThrow(new RuntimeException("Servico indisponivel"));
        when(propostaPocService.criar(any())).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));

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

        mockMvc.perform(post(URI_PROPOSTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void startupCancelarQuandoBuscarPorEmailFalhaProssegueParaService() throws Exception {
        UUID propostaId = UUID.randomUUID();
        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenThrow(new RuntimeException("Servico indisponivel"));
        when(propostaPocService.cancelar(propostaId)).thenReturn(criarPropostaResponseDTO(propostaId, UUID.randomUUID(), UUID.randomUUID(), StatusProposta.CANCELADA));

        mockMvc.perform(post(URI_CANCELAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void startupNaoPodeListarMinhasPassandoIdDeOutraStartupRetorna403() throws Exception {
        UUID minhaStartupId = UUID.randomUUID();
        UUID outraStartupId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(minhaStartupId, STARTUP_EMAIL));

        mockMvc.perform(get(URI_MINHAS)
                        .param(PARAM_STARTUP_ID, outraStartupId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void startupListandoMinhasComParametroQuandoBuscarPorEmailFalhaRetorna403() throws Exception {
        UUID outraStartupId = UUID.randomUUID();
        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenThrow(new RuntimeException("Falha na consulta"));

        mockMvc.perform(get(URI_MINHAS)
                        .param(PARAM_STARTUP_ID, outraStartupId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SuppressWarnings("unchecked")
    void controllerDiretoListarMinhasComAuthNulaRetorna401() {
        ObjectProvider<StartupService> provider = mock(ObjectProvider.class);
        PropostaPocController controller = new PropostaPocController(propostaPocService, provider);
        ResponseEntity<List<PropostaPocResponseDTO>> resp = controller.listarMinhas(null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void ambienteValidacaoConsultaFilaComSucessoRetorna200Ok() throws Exception {
        UUID ambienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL)).thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));
        when(desafioValidacaoService.listarPorAmbiente(ambienteId)).thenReturn(List.of(criarDesafioDTO(desafioId, ambienteId)));
        when(propostaPocService.listarFilaAmbiente(Set.of(desafioId))).thenReturn(List.of(
                criarPropostaResponseDTO(propostaId, UUID.randomUUID(), desafioId, StatusProposta.PENDENTE)
        ));

        mockMvc.perform(get(URI_FILA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(propostaId.toString()))
                .andExpect(jsonPath("$[0].status").value(STATUS_PENDENTE));
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void ambienteValidacaoTentaConsultarFilaDeOutroAmbienteRetorna403Forbidden() throws Exception {
        UUID ambienteId = UUID.randomUUID();
        UUID outroAmbienteId = UUID.randomUUID();

        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL)).thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));

        mockMvc.perform(get(URI_FILA)
                        .param(PARAM_AMBIENTE_ID, outroAmbienteId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void ambienteValidacaoConsultaFilaQuandoBuscarPorEmailFalhaRetorna403Forbidden() throws Exception {
        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL)).thenThrow(new RuntimeException("Falha na consulta"));

        mockMvc.perform(get(URI_FILA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void ambienteValidacaoConsultaFilaSemDesafiosRetornaListaVazia() throws Exception {
        UUID ambienteId = UUID.randomUUID();

        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL)).thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));
        when(desafioValidacaoService.listarPorAmbiente(ambienteId)).thenReturn(List.of());

        mockMvc.perform(get(URI_FILA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    void adminConsultaFilaDeAmbienteEspecificoRetorna200Ok() throws Exception {
        UUID ambienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(desafioValidacaoService.listarPorAmbiente(ambienteId)).thenReturn(List.of(criarDesafioDTO(desafioId, ambienteId)));
        when(propostaPocService.listarFilaAmbiente(Set.of(desafioId))).thenReturn(List.of(
                criarPropostaResponseDTO(propostaId, UUID.randomUUID(), desafioId, StatusProposta.PENDENTE)
        ));

        mockMvc.perform(get(URI_FILA)
                        .param(PARAM_AMBIENTE_ID, ambienteId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(propostaId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    void adminConsultaFilaSemParametroAmbienteRetornaListaVazia() throws Exception {
        mockMvc.perform(get(URI_FILA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void startupTentaConsultarFilaRetorna403Forbidden() throws Exception {
        mockMvc.perform(get(URI_FILA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultarFilaSemAutenticacaoRetorna401Unauthorized() throws Exception {
        mockMvc.perform(get(URI_FILA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SuppressWarnings("unchecked")
    void controllerDiretoConsultarFilaComAuthNulaRetorna401() {
        ObjectProvider<StartupService> startupProvider = mock(ObjectProvider.class);
        ObjectProvider<AmbienteValidacaoService> ambienteProvider = mock(ObjectProvider.class);
        ObjectProvider<DesafioValidacaoService> desafioProvider = mock(ObjectProvider.class);

        PropostaPocController controller = new PropostaPocController(
                propostaPocService, startupProvider, ambienteProvider, desafioProvider
        );
        ResponseEntity<List<PropostaPocResponseDTO>> resp = controller.consultarFila(null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void ambienteValidacaoPodeRecusarPropostaDeSeuDesafioRetorna200Ok() throws Exception {
        UUID ambienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();

        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL))
                .thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));
        when(propostaPocService.buscar(propostaId))
                .thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));
        when(desafioValidacaoService.buscar(desafioId))
                .thenReturn(criarDesafioDTO(desafioId, ambienteId));
        when(propostaPocService.recusar(propostaId, MOTIVO_RECUSA))
                .thenReturn(criarPropostaRecusadaResponseDTO(propostaId, startupId, desafioId, MOTIVO_RECUSA));

        RecusaPropostaRequestDTO request = new RecusaPropostaRequestDTO(MOTIVO_RECUSA);

        mockMvc.perform(post(URI_RECUSAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propostaId.toString()))
                .andExpect(jsonPath("$.status").value(STATUS_RECUSADA))
                .andExpect(jsonPath("$.motivoRecusa").value(MOTIVO_RECUSA));
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void ambienteValidacaoNaoPodeRecusarPropostaDeOutroAmbienteRetorna403Forbidden() throws Exception {
        UUID ambienteId = UUID.randomUUID();
        UUID outroAmbienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();

        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL))
                .thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));
        when(propostaPocService.buscar(propostaId))
                .thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));
        when(desafioValidacaoService.buscar(desafioId))
                .thenReturn(criarDesafioDTO(desafioId, outroAmbienteId));

        RecusaPropostaRequestDTO request = new RecusaPropostaRequestDTO(MOTIVO_RECUSA);

        mockMvc.perform(post(URI_RECUSAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    void adminPodeRecusarPropostaDeQualquerAmbienteRetorna200Ok() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        when(propostaPocService.recusar(propostaId, MOTIVO_RECUSA))
                .thenReturn(criarPropostaRecusadaResponseDTO(propostaId, startupId, desafioId, MOTIVO_RECUSA));

        RecusaPropostaRequestDTO request = new RecusaPropostaRequestDTO(MOTIVO_RECUSA);

        mockMvc.perform(post(URI_RECUSAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(STATUS_RECUSADA))
                .andExpect(jsonPath("$.motivoRecusa").value(MOTIVO_RECUSA));
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = STARTUP_EMAIL)
    void startupNaoPodeRecusarPropostaRetorna403Forbidden() throws Exception {
        UUID propostaId = UUID.randomUUID();
        RecusaPropostaRequestDTO request = new RecusaPropostaRequestDTO(MOTIVO_RECUSA);

        mockMvc.perform(post(URI_RECUSAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void recusarPropostaSemAutenticacaoRetorna401Unauthorized() throws Exception {
        UUID propostaId = UUID.randomUUID();
        RecusaPropostaRequestDTO request = new RecusaPropostaRequestDTO(MOTIVO_RECUSA);

        mockMvc.perform(post(URI_RECUSAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void recusarPropostaComMotivoVazioRetorna400BadRequest() throws Exception {
        UUID propostaId = UUID.randomUUID();
        RecusaPropostaRequestDTO request = new RecusaPropostaRequestDTO("   ");

        mockMvc.perform(post(URI_RECUSAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void ambienteRecusarQuandoBuscarPorEmailFalhaProssegueParaService() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL))
                .thenThrow(new RuntimeException("Falha ao buscar ambiente"));
        when(propostaPocService.recusar(propostaId, MOTIVO_RECUSA))
                .thenReturn(criarPropostaRecusadaResponseDTO(propostaId, startupId, desafioId, MOTIVO_RECUSA));

        RecusaPropostaRequestDTO request = new RecusaPropostaRequestDTO(MOTIVO_RECUSA);

        mockMvc.perform(post(URI_RECUSAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(STATUS_RECUSADA));
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void recusarPropostaComMotivoMaiorQue500CaracteresRetorna400BadRequest() throws Exception {
        UUID propostaId = UUID.randomUUID();
        String motivoLongo = "a".repeat(501);
        RecusaPropostaRequestDTO request = new RecusaPropostaRequestDTO(motivoLongo);

        mockMvc.perform(post(URI_RECUSAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    void recusarPropostaInexistenteRetorna404NotFound() throws Exception {
        UUID propostaId = UUID.randomUUID();
        when(propostaPocService.recusar(propostaId, MOTIVO_RECUSA))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta nao encontrada"));

        RecusaPropostaRequestDTO request = new RecusaPropostaRequestDTO(MOTIVO_RECUSA);

        mockMvc.perform(post(URI_RECUSAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = ADMIN_EMAIL)
    void recusarPropostaNaoPendenteRetorna400BadRequest() throws Exception {
        UUID propostaId = UUID.randomUUID();
        when(propostaPocService.recusar(propostaId, MOTIVO_RECUSA))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas propostas pendentes podem ser recusadas"));

        RecusaPropostaRequestDTO request = new RecusaPropostaRequestDTO(MOTIVO_RECUSA);

        mockMvc.perform(post(URI_RECUSAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void ambienteValidacaoConsultaFilaPassandoSeuProprioAmbienteIdRetorna200Ok() throws Exception {
        UUID ambienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL))
                .thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));
        when(desafioValidacaoService.listarPorAmbiente(ambienteId))
                .thenReturn(List.of(criarDesafioDTO(desafioId, ambienteId)));
        when(propostaPocService.listarFilaAmbiente(Set.of(desafioId)))
                .thenReturn(List.of(criarPropostaResponseDTO(propostaId, UUID.randomUUID(), desafioId, StatusProposta.PENDENTE)));

        mockMvc.perform(get(URI_FILA)
                        .param(PARAM_AMBIENTE_ID, ambienteId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(propostaId.toString()));
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO", username = AMBIENTE_EMAIL)
    void ambienteValidacaoConsultaFilaComMultiplosDesafiosRetornaTodasPropostas() throws Exception {
        UUID ambienteId = UUID.randomUUID();
        UUID d1 = UUID.randomUUID();
        UUID d2 = UUID.randomUUID();
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL))
                .thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));
        when(desafioValidacaoService.listarPorAmbiente(ambienteId))
                .thenReturn(List.of(criarDesafioDTO(d1, ambienteId), criarDesafioDTO(d2, ambienteId)));
        when(propostaPocService.listarFilaAmbiente(Set.of(d1, d2)))
                .thenReturn(List.of(
                        criarPropostaResponseDTO(p1, UUID.randomUUID(), d1, StatusProposta.PENDENTE),
                        criarPropostaResponseDTO(p2, UUID.randomUUID(), d2, StatusProposta.PENDENTE)
                ));

        mockMvc.perform(get(URI_FILA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(p1.toString()))
                .andExpect(jsonPath("$[1].id").value(p2.toString()));
    }

    @Test
    void controllerDiretoRecusarComProvidersNulos() {
        PropostaPocController controller = new PropostaPocController(propostaPocService, null, null, null);
        UUID id = UUID.randomUUID();
        when(propostaPocService.recusar(id, MOTIVO_RECUSA))
                .thenReturn(criarPropostaRecusadaResponseDTO(id, UUID.randomUUID(), UUID.randomUUID(), MOTIVO_RECUSA));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "parceiro@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));

        ResponseEntity<PropostaPocResponseDTO> resp = controller.recusar(
                id, new RecusaPropostaRequestDTO(MOTIVO_RECUSA), auth);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void controllerDiretoConsultarFilaComProvidersNulos() {
        PropostaPocController controller = new PropostaPocController(propostaPocService, null, null, null);
        UUID ambienteId = UUID.randomUUID();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "parceiro@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));

        ResponseEntity<List<PropostaPocResponseDTO>> resp = controller.consultarFila(ambienteId, auth);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
    }

    private TermoPiloto criarTermoPiloto(UUID id, UUID propostaId, UUID startupId, UUID desafioId, UUID ambienteId) {
        TermoPiloto termo = new TermoPiloto();
        termo.setId(id);
        termo.setPropostaPocId(propostaId);
        termo.setStartupId(startupId);
        termo.setDesafioValidacaoId(desafioId);
        termo.setAmbienteValidacaoId(ambienteId);
        termo.setEscopoTeste(ESCOPO_TESTE);
        termo.setDuracaoDias(30);
        termo.setResponsabilidadesStartup(RESP_STARTUP);
        termo.setResponsabilidadesAmbiente(RESP_AMBIENTE);
        termo.setMetricaValidacao(TAXA_SUPERIOR_90);
        termo.setDataCriacao(LocalDateTime.now());
        return termo;
    }

    @Test
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void ambienteProprietarioPodeAceitarPropostaComSucessoERevelarIdentidade() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        AmbienteValidacaoResponseDTO ambienteDTO = criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL);
        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL)).thenReturn(ambienteDTO);
        when(ambienteValidacaoService.buscar(ambienteId)).thenReturn(ambienteDTO);

        PropostaPocResponseDTO propostaDTO = criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE);
        when(propostaPocService.buscar(propostaId)).thenReturn(propostaDTO);
        when(desafioValidacaoService.buscar(desafioId)).thenReturn(criarDesafioDTO(desafioId, ambienteId));

        TermoPiloto termo = criarTermoPiloto(UUID.randomUUID(), propostaId, startupId, desafioId, ambienteId);
        when(propostaPocService.aceitar(propostaId, ambienteId)).thenReturn(termo);

        mockMvc.perform(post(URI_ACEITAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusProposta").value("EM_TESTE"))
                .andExpect(jsonPath("$.propostaPocId").value(propostaId.toString()))
                .andExpect(jsonPath("$.startupId").value(startupId.toString()))
                .andExpect(jsonPath("$.ambienteValidacaoId").value(ambienteId.toString()))
                .andExpect(jsonPath("$.razaoSocial").value("Hospital Esperanca"))
                .andExpect(jsonPath("$.nomeFantasia").value("Hospital Esperanca PE"))
                .andExpect(jsonPath("$.emailContato").value(AMBIENTE_EMAIL))
                .andExpect(jsonPath("$.telefoneContato").value("81988887777"))
                .andExpect(jsonPath("$.escopoTeste").value(ESCOPO_TESTE))
                .andExpect(jsonPath("$.duracaoDias").value(30))
                .andExpect(jsonPath("$.metricaValidacao").value(TAXA_SUPERIOR_90));
    }

    @Test
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void ambienteDeOutroDesafioNaoPodeAceitarPropostaRetornaForbidden() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID meuAmbienteId = UUID.randomUUID();
        UUID outroAmbienteId = UUID.randomUUID();

        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL))
                .thenReturn(criarAmbienteDTO(meuAmbienteId, AMBIENTE_EMAIL));

        PropostaPocResponseDTO propostaDTO = criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE);
        when(propostaPocService.buscar(propostaId)).thenReturn(propostaDTO);
        when(desafioValidacaoService.buscar(desafioId)).thenReturn(criarDesafioDTO(desafioId, outroAmbienteId));

        mockMvc.perform(post(URI_ACEITAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = {"ADMIN"})
    void adminPodeAceitarPropostaDeQualquerDesafioComSucesso() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        AmbienteValidacaoResponseDTO ambienteDTO = criarAmbienteDTO(ambienteId, "parceiro@azreal.com");
        when(ambienteValidacaoService.buscar(ambienteId)).thenReturn(ambienteDTO);

        PropostaPocResponseDTO propostaDTO = criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE);
        when(propostaPocService.buscar(propostaId)).thenReturn(propostaDTO);
        when(desafioValidacaoService.buscar(desafioId)).thenReturn(criarDesafioDTO(desafioId, ambienteId));

        TermoPiloto termo = criarTermoPiloto(UUID.randomUUID(), propostaId, startupId, desafioId, ambienteId);
        when(propostaPocService.aceitar(propostaId, ambienteId)).thenReturn(termo);

        mockMvc.perform(post(URI_ACEITAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusProposta").value("EM_TESTE"))
                .andExpect(jsonPath("$.razaoSocial").value("Hospital Esperanca"));
    }

    @Test
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void startupNaoPodeAceitarPropostaRetornaForbidden() throws Exception {
        UUID propostaId = UUID.randomUUID();
        mockMvc.perform(post(URI_ACEITAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonimoNaoPodeAceitarPropostaRetornaUnauthorized() throws Exception {
        UUID propostaId = UUID.randomUUID();
        mockMvc.perform(post(URI_ACEITAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void aceitarPropostaInexistenteRetornaNotFound() throws Exception {
        UUID propostaId = UUID.randomUUID();
        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL))
                .thenReturn(criarAmbienteDTO(UUID.randomUUID(), AMBIENTE_EMAIL));
        when(propostaPocService.buscar(propostaId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta nao encontrada"));
        when(propostaPocService.aceitar(any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta nao encontrada"));

        mockMvc.perform(post(URI_ACEITAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void aceitarPropostaNaoPendenteRetornaBadRequest() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL))
                .thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));
        when(propostaPocService.buscar(propostaId))
                .thenReturn(criarPropostaResponseDTO(propostaId, UUID.randomUUID(), desafioId, StatusProposta.RECUSADA));
        when(desafioValidacaoService.buscar(desafioId))
                .thenReturn(criarDesafioDTO(desafioId, ambienteId));
        when(propostaPocService.aceitar(propostaId, ambienteId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas propostas pendentes podem ser aceitas"));

        mockMvc.perform(post(URI_ACEITAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void startupProprietariaPodeConsultarTermoDaProposta() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        when(startupService.buscarPorEmail(STARTUP_EMAIL))
                .thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));
        when(ambienteValidacaoService.buscar(ambienteId))
                .thenReturn(criarAmbienteDTO(ambienteId, AMBIENTE_EMAIL));

        TermoPiloto termo = criarTermoPiloto(UUID.randomUUID(), propostaId, startupId, desafioId, ambienteId);
        when(propostaPocService.obterTermoPorPropostaId(propostaId)).thenReturn(termo);
        TermoPilotoResponseDTO termoDTO = new TermoPilotoResponseDTO(
                UUID.randomUUID(), propostaId, startupId, desafioId, ambienteId,
                ESCOPO_TESTE, 30, RESP_STARTUP, RESP_AMBIENTE, TAXA_SUPERIOR_90,
                LocalDateTime.now(), StatusProposta.EM_TESTE, "Hospital Esperanca",
                "Hospital Esperanca PE", AMBIENTE_EMAIL, "81988887777", "Saude", PorteOrganizacao.GRANDE
        );
        when(termoPilotoService.obterTermoComIdentidadeRevelada(any(), any())).thenReturn(termoDTO);

        mockMvc.perform(get(URI_TERMO, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propostaPocId").value(propostaId.toString()))
                .andExpect(jsonPath("$.razaoSocial").value("Hospital Esperanca"));
    }

    @Test
    @WithMockUser(username = OUTRA_EMAIL, roles = {"STARTUP"})
    void outraStartupNaoPodeConsultarTermoDaPropostaRetornaForbidden() throws Exception {
        UUID propostaId = UUID.randomUUID();
        when(termoPilotoService.obterTermoComIdentidadeRevelada(any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get(URI_TERMO, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void ambienteProprietarioPodeConsultarTermoDaProposta() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPilotoResponseDTO termoDTO = new TermoPilotoResponseDTO(
                UUID.randomUUID(), propostaId, startupId, UUID.randomUUID(), ambienteId,
                ESCOPO_TESTE, 30, RESP_STARTUP, RESP_AMBIENTE, TAXA_SUPERIOR_90,
                LocalDateTime.now(), StatusProposta.EM_TESTE, "Hospital Esperanca",
                "Hospital Esperanca PE", AMBIENTE_EMAIL, "81988887777", "Saude", PorteOrganizacao.GRANDE
        );
        when(termoPilotoService.obterTermoComIdentidadeRevelada(any(), any())).thenReturn(termoDTO);

        mockMvc.perform(get(URI_TERMO, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambienteValidacaoId").value(ambienteId.toString()));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = {"ADMIN"})
    void adminPodeConsultarTermoDeQualquerProposta() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPilotoResponseDTO termoDTO = new TermoPilotoResponseDTO(
                UUID.randomUUID(), propostaId, UUID.randomUUID(), UUID.randomUUID(), ambienteId,
                ESCOPO_TESTE, 30, RESP_STARTUP, RESP_AMBIENTE, TAXA_SUPERIOR_90,
                LocalDateTime.now(), StatusProposta.EM_TESTE, "Hospital Esperanca",
                "Hospital Esperanca PE", "outro@email.com", "81988887777", "Saude", PorteOrganizacao.GRANDE
        );
        when(termoPilotoService.obterTermoComIdentidadeRevelada(any(), any())).thenReturn(termoDTO);

        mockMvc.perform(get(URI_TERMO, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propostaPocId").value(propostaId.toString()));
    }

    @Test
    void controllerDiretoAceitarComProvidersNulos() {
        PropostaPocController controller = new PropostaPocController(propostaPocService, null, null, null);
        UUID id = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        TermoPiloto termo = criarTermoPiloto(UUID.randomUUID(), id, UUID.randomUUID(), UUID.randomUUID(), ambienteId);
        when(propostaPocService.aceitar(id, null)).thenReturn(termo);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "parceiro@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));

        ResponseEntity<TermoPilotoResponseDTO> resp = controller.aceitar(id, auth);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void controllerDiretoBuscarTermoComProvidersNulos() {
        PropostaPocController controller = new PropostaPocController(propostaPocService, null, null, null, null);
        UUID id = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        TermoPiloto termo = criarTermoPiloto(UUID.randomUUID(), id, UUID.randomUUID(), UUID.randomUUID(), ambienteId);
        when(propostaPocService.obterTermoPorPropostaId(id)).thenReturn(termo);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                ADMIN_EMAIL, "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        ResponseEntity<TermoPilotoResponseDTO> resp = controller.buscarTermoDaProposta(id, auth);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void controllerDiretoRegistrarEntregaEConfirmacao() {
        ObjectProvider<TermoPilotoService> termoProvider = mock(ObjectProvider.class);
        TermoPilotoService termoService = mock(TermoPilotoService.class);
        when(termoProvider.getIfAvailable()).thenReturn(termoService);

        PropostaPocController controller = new PropostaPocController(
                propostaPocService, null, null, null, termoProvider);

        UUID id = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        TermoPiloto termo = criarTermoPiloto(UUID.randomUUID(), id, UUID.randomUUID(), UUID.randomUUID(), ambienteId);
        termo.setStatus(StatusProposta.EM_TESTE);

        EntregaPilotoRequestDTO entregaReq = new EntregaPilotoRequestDTO("Resumo", null, null);
        when(termoService.registrarEntrega(eq(id), eq(entregaReq), any())).thenReturn(termo);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                ADMIN_EMAIL, "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        ResponseEntity<TermoPilotoResponseDTO> entregaResp = controller.registrarEntrega(id, entregaReq, auth);
        assertEquals(HttpStatus.OK, entregaResp.getStatusCode());

        ConfirmacaoExecucaoRequestDTO confReq = new ConfirmacaoExecucaoRequestDTO(
                true, AvaliacaoMetrica.ATENDEU_TOTALMENTE, "Parecer", ModoPrivacidadePoc.EXIBICAO_NOMINAL, null, null);
        termo.confirmarExecucao(true, AvaliacaoMetrica.ATENDEU_TOTALMENTE, "Parecer", ModoPrivacidadePoc.EXIBICAO_NOMINAL, null, null);
        when(termoService.confirmarExecucao(eq(id), eq(confReq), any())).thenReturn(termo);

        ResponseEntity<ConfirmacaoExecucaoResponseDTO> confResp = controller.confirmarResultado(id, confReq, auth);
        assertEquals(HttpStatus.OK, confResp.getStatusCode());
        assertEquals(StatusProposta.POC_CONCLUIDA, confResp.getBody().status());
    }

    @Test
    void controllerDiretoRegistrarEntregaEConfirmacaoServicoIndisponivel() {
        PropostaPocController controller = new PropostaPocController(propostaPocService, null, null, null, null);
        UUID id = UUID.randomUUID();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                ADMIN_EMAIL, "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        EntregaPilotoRequestDTO entregaReq = new EntregaPilotoRequestDTO("Resumo", null, null);
        assertThrows(ResponseStatusException.class, () -> controller.registrarEntrega(id, entregaReq, auth));

        ConfirmacaoExecucaoRequestDTO confReq = new ConfirmacaoExecucaoRequestDTO(false, null, null, null, null, null);
        assertThrows(ResponseStatusException.class, () -> controller.confirmarResultado(id, confReq, auth));
    }
}
