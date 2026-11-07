package br.senac.fullstack.controller;

import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.dto.DesafioRecomendadoDTO;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.Startup;
import br.senac.fullstack.model.StatusDesafio;
import br.senac.fullstack.service.MatchmakingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchmakingController.class)
class MatchmakingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchmakingService matchmakingService;

    private DesafioRecomendadoDTO criarDesafioRecomendadoDTO() {
        UUID desafioId = UUID.randomUUID();
        DesafioAnonimoResponseDTO anonimo = new DesafioAnonimoResponseDTO(
                desafioId,
                "Integracao de Totem de Triagem",
                "Fila excessiva de espera na recepcao",
                "Saude",
                PorteOrganizacao.GRANDE,
                "PE",
                "Recife",
                ModalidadeTeste.PRESENCIAL,
                "Pacientes ambulatoriais",
                120,
                30,
                StatusDesafio.ABERTO,
                LocalDateTime.now()
        );
        return DesafioRecomendadoDTO.de(anonimo, "Compatibilidade semantica comprovada no setor hospitalar.", "IA");
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = "nook@senac.br")
    void startupAutenticadaObtemDesafiosCompativeisIsolandoPrivacidadeRnf04() throws Exception {
        Startup startup = new Startup();
        startup.setId(UUID.randomUUID());
        startup.setEmail("nook@senac.br");

        when(matchmakingService.obterStartupPorEmail("nook@senac.br")).thenReturn(startup);
        when(matchmakingService.recomendarDesafiosParaStartup(any(), any())).thenReturn(List.of(criarDesafioRecomendadoDTO()));

        mockMvc.perform(get("/matchmaking/desafios-compativeis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Integracao de Totem de Triagem"))
                .andExpect(jsonPath("$[0].setor").value("Saude"))
                .andExpect(jsonPath("$[0].motivo").value("Compatibilidade semantica comprovada no setor hospitalar."))
                .andExpect(jsonPath("$[0].origemRecomendacao").value("IA"))
                // Isolamento de privacidade RNF04
                .andExpect(jsonPath("$[0].razaoSocial").doesNotExist())
                .andExpect(jsonPath("$[0].nomeFantasia").doesNotExist())
                .andExpect(jsonPath("$[0].emailContato").doesNotExist())
                .andExpect(jsonPath("$[0].telefoneContato").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "AMBIENTE_VALIDACAO")
    void ambienteValidacaoRecebe403AoTentarAcessarEndpointDeStartup() throws Exception {
        mockMvc.perform(get("/matchmaking/desafios-compativeis"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@senac.br")
    void adminPodeConsultarMatchmakingDeQualquerStartup() throws Exception {
        UUID startupId = UUID.randomUUID();
        Startup startup = new Startup();
        startup.setId(startupId);
        startup.setEmail("alvo@startup.com");

        when(matchmakingService.obterStartup(startupId)).thenReturn(startup);
        when(matchmakingService.recomendarDesafiosParaStartup(any(), any())).thenReturn(List.of(criarDesafioRecomendadoDTO()));

        mockMvc.perform(get("/matchmaking/desafios-compativeis")
                        .param("startupId", startupId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").exists());
    }

    @Test
    @WithMockUser(roles = "STARTUP", username = "invasora@startup.com")
    void startupRecebe403AoConsultarIdDeOutraStartup() throws Exception {
        UUID startupId = UUID.randomUUID();
        Startup startupAlvo = new Startup();
        startupAlvo.setId(startupId);
        startupAlvo.setEmail("vitima@startup.com");

        when(matchmakingService.obterStartup(startupId)).thenReturn(startupAlvo);

        mockMvc.perform(get("/matchmaking/desafios-compativeis")
                        .param("startupId", startupId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioSemAutenticacaoRecebe401() throws Exception {
        mockMvc.perform(get("/matchmaking/desafios-compativeis"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void controllerDiretoComAuthNulaRetorna401() {
        MatchmakingController controller = new MatchmakingController(matchmakingService);
        ResponseEntity<List<DesafioRecomendadoDTO>> resp = controller.obterDesafiosCompativeis(null, null);
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }
}
