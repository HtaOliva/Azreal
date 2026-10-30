package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.FilaItemResponseDTO;
import br.senac.fullstack.dto.MentorRequestDTO;
import br.senac.fullstack.dto.MentorResponseDTO;
import br.senac.fullstack.dto.RespostaPedidoRequestDTO;
import br.senac.fullstack.dto.SolicitacaoRequestDTO;
import br.senac.fullstack.dto.SolicitacaoResponseDTO;
import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.StatusSolicitacao;
import br.senac.fullstack.service.AceiteTermoService;
import br.senac.fullstack.service.MentorService;
import br.senac.fullstack.service.NullObjectProvider;
import br.senac.fullstack.service.SolicitacaoService;
import br.senac.fullstack.service.StartupService;
import br.senac.fullstack.service.VinculoService;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SolicitacaoController.class)
class SolicitacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SolicitacaoService solicitacaoService;

    @MockBean
    private MentorService mentorService;

    @Test
    void semAutenticacaoRecebe401AoCriarPedido() throws Exception {
        mockMvc.perform(post("/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MENTOR")
    void mentorRecebe403AoCriarPedido() throws Exception {
        String body = "{\"startupId\":\"" + UUID.randomUUID()
                + "\",\"mentorId\":\"" + UUID.randomUUID() + "\",\"assunto\":\"teste\"}";
        mockMvc.perform(post("/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STARTUP")
    void startupRecebe403AoResponderPedido() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/solicitacoes/{id}/responder", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aceitar\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void criarFilaEResponderNoControllerFuncionam() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        var aceite = aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1");
        StartupService startupServiceReal = new StartupService(new NullObjectProvider<>(), aceiteService);
        MentorService mentorServiceReal = new MentorService(new NullObjectProvider<>(), aceiteService);
        VinculoService vinculoServiceReal = new VinculoService(new NullObjectProvider<>(), mentorServiceReal);
        SolicitacaoService solicitacaoServiceReal = new SolicitacaoService(
                new NullObjectProvider<>(), startupServiceReal, mentorServiceReal, vinculoServiceReal);

        SolicitacaoController controller = new SolicitacaoController(solicitacaoServiceReal, mentorServiceReal);

        StartupResponseDTO startup = startupServiceReal.criar(new StartupRequestDTO(
                "Startup Sol", "sol@senac.br", "senha123", "Desc", "EdTech",
                LocalDate.now(), Estagio.IDEACAO, List.of("Bruno"), "SP", "BR", false, null, aceite.id()));

        MentorResponseDTO mentor = mentorServiceReal.criar(new MentorRequestDTO(
                "Mentor Sol", "mentorsol@senac.br", "senha123",
                List.of("Vendas"), List.of("B2B"), "8 anos",
                "Semanal", "SP", "11999999999", "https://linkedin.com/in/msol", aceite.id()));

        SolicitacaoRequestDTO req = new SolicitacaoRequestDTO(startup.id(), mentor.id(), "Mentoria em B2B");
        ResponseEntity<SolicitacaoResponseDTO> criada = controller.criar(req);
        assertEquals(HttpStatus.CREATED, criada.getStatusCode());
        assertNotNull(criada.getBody());

        ResponseEntity<List<FilaItemResponseDTO>> fila = controller.fila(mentor.id());
        assertEquals(HttpStatus.OK, fila.getStatusCode());
        assertFalse(fila.getBody().isEmpty());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "mentorsol@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_MENTOR")));
        RespostaPedidoRequestDTO respReq = new RespostaPedidoRequestDTO(true, "Aceito orientar");
        ResponseEntity<SolicitacaoResponseDTO> respondida = controller.responder(criada.getBody().id(), respReq, auth);
        assertEquals(HttpStatus.OK, respondida.getStatusCode());
        assertEquals(StatusSolicitacao.ACEITO, respondida.getBody().status());
    }
}
