package br.senac.fullstack.controller;

import br.senac.fullstack.service.IncubadoraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncubadoraController.class)
class IncubadoraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncubadoraService incubadoraService;

    @Test
    void listarAtivasRetorna200() throws Exception {
        mockMvc.perform(get("/incubadoras"))
                .andExpect(status().isOk());
    }

    @Test
    void semAutenticacaoRecebe401AoCriarIncubadora() throws Exception {
        mockMvc.perform(post("/incubadoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inc X\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STARTUP")
    void startupRecebe403AoCriarIncubadora() throws Exception {
        mockMvc.perform(post("/incubadoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Inc X\"}"))
                .andExpect(status().isForbidden());
    }
}
