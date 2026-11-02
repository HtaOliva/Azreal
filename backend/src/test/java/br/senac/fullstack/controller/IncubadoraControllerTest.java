package br.senac.fullstack.controller;

import br.senac.fullstack.dto.IncubadoraRequestDTO;
import br.senac.fullstack.dto.IncubadoraResponseDTO;
import br.senac.fullstack.service.IncubadoraService;
import br.senac.fullstack.service.NullObjectProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void operacoesDiretasNoControllerFuncionam() {
        IncubadoraService serviceReal = new IncubadoraService(new NullObjectProvider<>());
        IncubadoraController controller = new IncubadoraController(serviceReal);

        IncubadoraRequestDTO req = new IncubadoraRequestDTO(
                "Incubadora Senac", "IA", "Pitch", "SP", List.of("Nacional"),
                "Hibrido", "Descricao", "https://senac.br", true);

        ResponseEntity<IncubadoraResponseDTO> criada = controller.criar(req);
        assertEquals(HttpStatus.CREATED, criada.getStatusCode());
        assertNotNull(criada.getBody());

        ResponseEntity<List<IncubadoraResponseDTO>> ativas = controller.listarAtivas();
        assertEquals(HttpStatus.OK, ativas.getStatusCode());
        assertFalse(ativas.getBody().isEmpty());

        ResponseEntity<List<IncubadoraResponseDTO>> todas = controller.listarTodos();
        assertEquals(HttpStatus.OK, todas.getStatusCode());
        assertFalse(todas.getBody().isEmpty());

        IncubadoraRequestDTO updateReq = new IncubadoraRequestDTO(
                "Incubadora Atualizada", "Cloud", "MVP", "RJ", List.of("Regional"),
                "Remoto", "Nova Desc", "https://senac.br/2", true);
        ResponseEntity<IncubadoraResponseDTO> atualizada = controller.atualizar(criada.getBody().id(), updateReq);
        assertEquals(HttpStatus.OK, atualizada.getStatusCode());
        assertEquals("Incubadora Atualizada", atualizada.getBody().nome());

        ResponseEntity<Void> inativada = controller.definirAtivo(criada.getBody().id(), false);
        assertEquals(HttpStatus.NO_CONTENT, inativada.getStatusCode());
    }
}
