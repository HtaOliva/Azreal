package br.senac.fullstack.controller;

import br.senac.fullstack.service.CertificadoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CertificadoController.class)
class CertificadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CertificadoService certificadoService;

    @Test
    void semAutenticacaoRecebe401AoListarCertificados() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/startups/{id}/certificados", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STARTUP")
    void startupListaCertificadosRetorna200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/startups/{id}/certificados", UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}
