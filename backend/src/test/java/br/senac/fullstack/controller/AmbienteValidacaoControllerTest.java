package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.AmbienteValidacaoRequestDTO;
import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.AmbienteValidacaoUpdateDTO;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.service.AceiteTermoService;
import br.senac.fullstack.service.AmbienteValidacaoService;
import br.senac.fullstack.service.NullObjectProvider;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AmbienteValidacaoController.class)
class AmbienteValidacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AmbienteValidacaoService ambienteValidacaoService;

    @Test
    @WithMockUser(roles = "STARTUP")
    void startupNaoPodeConsultarNemAtualizarAmbienteDiretamenteRnf04() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/ambientes-validacao/{id}", id))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/ambientes-validacao/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    private record Contexto(AmbienteValidacaoController controller, AmbienteValidacaoResponseDTO ambiente) { }

    private Contexto criarContexto() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        var aceite = aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1");
        AmbienteValidacaoService serviceReal = new AmbienteValidacaoService(new NullObjectProvider<>(), aceiteService);
        AmbienteValidacaoController controller = new AmbienteValidacaoController(serviceReal);

        AmbienteValidacaoRequestDTO req = new AmbienteValidacaoRequestDTO(
                "Instituto Senac PE", "Senac Pernambuco", "Educacao",
                PorteOrganizacao.INSTITUICAO_ENSINO_SAUDE, "PE", "Recife",
                "parcerias@pe.senac.br", "8134136666", "https://linkedin.com/company/senacpe",
                "senha123", aceite.id());
        ResponseEntity<AmbienteValidacaoResponseDTO> criado = controller.criar(req);
        return new Contexto(controller, criado.getBody());
    }

    @Test
    void criarBuscarListarEAtualizarComPermissoesFuncionam() {
        Contexto ctx = criarContexto();
        assertNotNull(ctx.ambiente());

        Authentication authDono = new UsernamePasswordAuthenticationToken(
                "parcerias@pe.senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));
        Authentication authAdmin = new UsernamePasswordAuthenticationToken(
                "admin@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication authOutro = new UsernamePasswordAuthenticationToken(
                "outro@empresa.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));

        assertEquals(HttpStatus.OK, ctx.controller().buscar(ctx.ambiente().id(), authDono).getStatusCode());
        assertEquals(HttpStatus.OK, ctx.controller().buscar(ctx.ambiente().id(), authAdmin).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, ctx.controller().buscar(ctx.ambiente().id(), authOutro).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, ctx.controller().buscar(ctx.ambiente().id(), null).getStatusCode());

        assertFalse(ctx.controller().listar().getBody().isEmpty());

        AmbienteValidacaoUpdateDTO update = new AmbienteValidacaoUpdateDTO(
                null, "Senac Recife Hub", null, null, null, null, null, null, null);
        ResponseEntity<AmbienteValidacaoResponseDTO> atualizado =
                ctx.controller().atualizar(ctx.ambiente().id(), update, authDono);
        assertEquals(HttpStatus.OK, atualizado.getStatusCode());
        assertEquals("Senac Recife Hub", atualizado.getBody().nomeFantasia());

        assertEquals(HttpStatus.FORBIDDEN,
                ctx.controller().atualizar(ctx.ambiente().id(), update, authOutro).getStatusCode());
    }
}
