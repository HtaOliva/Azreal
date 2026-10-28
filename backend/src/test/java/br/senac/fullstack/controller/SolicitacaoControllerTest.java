package br.senac.fullstack.controller;

import br.senac.fullstack.service.MentorService;
import br.senac.fullstack.service.SolicitacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

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
        String body = "{\"startupId\":\"" + java.util.UUID.randomUUID() + "\",\"mentorId\":\"" + java.util.UUID.randomUUID() + "\",\"assunto\":\"teste\"}";
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
}
