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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PropostaPocController.class)
class PropostaPocControllerTest {

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
                "Escopo do teste de telemetria",
                30,
                "Resp startup",
                "Resp ambiente",
                "Disponibilidade > 99%",
                true,
                status,
                LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = "startup@azreal.com")
    void startupAutenticadaSubmetePropostaComSucesso() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail("startup@azreal.com")).thenReturn(criarStartupDTO(startupId, "startup@azreal.com"));

        PropostaPocRequestDTO request = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                "Validacao da acuracia",
                15,
                "Resp startup",
                "Resp ambiente",
                "Taxa > 90%",
                true
        );

        when(propostaPocService.criar(any())).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));

        mockMvc.perform(post("/propostas-poc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(propostaId.toString()))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.escopoTeste").value("Escopo do teste de telemetria"));
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
                "Resp startup",
                "Resp ambiente",
                "Taxa > 90%",
                true
        );

        mockMvc.perform(post("/propostas-poc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = "startup@azreal.com")
    void submissaoSemAceitePrevioRetornaBadRequest400() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocRequestDTO requestInvalido = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                "Escopo",
                15,
                "Resp",
                "Resp",
                "Metrica",
                false
        );

        mockMvc.perform(post("/propostas-poc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = "startup@azreal.com")
    void startupCancelaPropostaPendenteComSucesso() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail("startup@azreal.com")).thenReturn(criarStartupDTO(startupId, "startup@azreal.com"));
        when(propostaPocService.buscar(propostaId)).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));
        when(propostaPocService.cancelar(propostaId)).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.CANCELADA));

        mockMvc.perform(post("/propostas-poc/{id}/cancelar", propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propostaId.toString()))
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = "outra@azreal.com")
    void startupNaoPodeCancelarPropostaDeOutraStartup() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID outraStartupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail("outra@azreal.com")).thenReturn(criarStartupDTO(outraStartupId, "outra@azreal.com"));
        when(propostaPocService.buscar(propostaId)).thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));

        mockMvc.perform(post("/propostas-poc/{id}/cancelar", propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = "startup@azreal.com")
    void cancelarPropostaNaoPendenteRetorna400BadRequest() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail("startup@azreal.com")).thenReturn(criarStartupDTO(startupId, "startup@azreal.com"));
        when(propostaPocService.buscar(propostaId)).thenReturn(criarPropostaResponseDTO(propostaId, startupId, UUID.randomUUID(), StatusProposta.EM_TESTE));
        when(propostaPocService.cancelar(propostaId)).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas propostas pendentes podem ser canceladas"));

        mockMvc.perform(post("/propostas-poc/{id}/cancelar", propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = "startup@azreal.com")
    void listarMinhasPropostasRetornaPropostasDaStartupAutenticada() throws Exception {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();

        when(startupService.buscarPorEmail("startup@azreal.com")).thenReturn(criarStartupDTO(startupId, "startup@azreal.com"));
        when(propostaPocService.listarPorStartup(startupId)).thenReturn(List.of(
                criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE)
        ));

        mockMvc.perform(get("/propostas-poc/minhas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(propostaId.toString()))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].startupId").value(startupId.toString()));
    }

    @Test
    void listarMinhasSemAutenticacaoRetorna401Unauthorized() throws Exception {
        mockMvc.perform(get("/propostas-poc/minhas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO")
    void listarMinhasComPerfilInvalidoRetorna403Forbidden() throws Exception {
        mockMvc.perform(get("/propostas-poc/minhas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarPropostaPorIdRetorna200Ok() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        when(propostaPocService.buscar(eq(propostaId)))
                .thenReturn(criarPropostaResponseDTO(propostaId, startupId, desafioId, StatusProposta.PENDENTE));

        mockMvc.perform(get("/propostas-poc/{id}", propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propostaId.toString()))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }
}
