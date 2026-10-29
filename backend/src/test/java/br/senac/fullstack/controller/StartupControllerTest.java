package br.senac.fullstack.controller;

import br.senac.fullstack.service.StartupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StartupController.class)
class StartupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StartupService startupService;

    @Test
    @WithMockUser(roles = "MENTOR")
    void mentorRecebe403AoAtualizarStartup() throws Exception {
        UUID id = UUID.randomUUID();
        when(startupService.atualizar(any(), any())).thenReturn(null);

        mockMvc.perform(put("/startups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void semAutenticacaoRecebe401AoAtualizarStartup() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/startups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
