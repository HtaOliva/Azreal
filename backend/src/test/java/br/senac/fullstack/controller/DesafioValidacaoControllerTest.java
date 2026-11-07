package br.senac.fullstack.controller;

import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoRequestDTO;
import br.senac.fullstack.dto.DesafioValidacaoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoUpdateDTO;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusDesafio;
import br.senac.fullstack.service.DesafioValidacaoService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DesafioValidacaoController.class)
class DesafioValidacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DesafioValidacaoService desafioValidacaoService;

    // =========================================================================
    // MockMvc Tests: Validações de RBAC e Isolamento de Privacidade (RNF04)
    // =========================================================================

    @Test
    @WithMockUser(roles = "STARTUP")
    void startupRecebe403AoTentarCriarDesafio() throws Exception {
        mockMvc.perform(post("/desafios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "ambienteValidacaoId": "00000000-0000-0000-0000-000000000001",
                                    "titulo": "Titulo Desafio",
                                    "descricaoProblema": "Problema",
                                    "modalidade": "REMOTO",
                                    "duracaoMaxima": 30
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STARTUP")
    void startupRecebe403AoTentarAtualizarDesafio() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/desafios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STARTUP")
    void startupRecebe403AoTentarInativarDesafio() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/desafios/{id}/inativar", id))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STARTUP")
    void startupRecebe403AoTentarConsultarDesafioDetalhadoDiretamente() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/desafios/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STARTUP")
    void startupRecebeRespostaAnonimaEmGetDesafiosSemExposicaoDeIdentidade() throws Exception {
        UUID id = UUID.randomUUID();
        DesafioAnonimoResponseDTO anonimo = new DesafioAnonimoResponseDTO(
                id,
                "Triagem com IA",
                "Fila excessiva de espera",
                "Saude",
                PorteOrganizacao.GRANDE,
                "PE",
                "Recife",
                ModalidadeTeste.PRESENCIAL,
                "Pacientes ambulatoriais",
                100,
                30,
                StatusDesafio.ABERTO,
                LocalDateTime.now()
        );

        when(desafioValidacaoService.listarAtivosAnonimos()).thenReturn(List.of(anonimo));

        mockMvc.perform(get("/desafios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].titulo").value("Triagem com IA"))
                .andExpect(jsonPath("$[0].setor").value("Saude"))
                .andExpect(jsonPath("$[0].porte").value("GRANDE"))
                .andExpect(jsonPath("$[0].ufIbge").value("PE"))
                .andExpect(jsonPath("$[0].municipioIbge").value("Recife"))
                .andExpect(jsonPath("$[0].modalidade").value("PRESENCIAL"))
                .andExpect(jsonPath("$[0].razaoSocial").doesNotExist())
                .andExpect(jsonPath("$[0].nomeFantasia").doesNotExist())
                .andExpect(jsonPath("$[0].emailContato").doesNotExist())
                .andExpect(jsonPath("$[0].telefoneContato").doesNotExist());
    }

    @Test
    void publicoAnonimoConsegueAcessarGetDesafiosComSucesso() throws Exception {
        when(desafioValidacaoService.listarAtivosAnonimos()).thenReturn(List.of());

        mockMvc.perform(get("/desafios"))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // Programmatic Context Tests: Ownership e Operações In-Memory
    // =========================================================================

    private record Contexto(DesafioValidacaoController controller, DesafioValidacaoResponseDTO desafio) { }

    private Contexto criarContexto() {
        DesafioValidacaoService serviceReal = new DesafioValidacaoService(
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new NullObjectProvider<>()
        );
        DesafioValidacaoController controller = new DesafioValidacaoController(serviceReal);

        UUID ambienteId = UUID.randomUUID();
        DesafioValidacaoRequestDTO req = new DesafioValidacaoRequestDTO(
                ambienteId,
                "Triagem Automatizada com IA",
                "Fila excessiva na recepção do hospital",
                "Pacientes ambulatoriais",
                120,
                ModalidadeTeste.PRESENCIAL,
                30
        );
        ResponseEntity<DesafioValidacaoResponseDTO> criado = controller.criar(req);
        return new Contexto(controller, criado.getBody());
    }

    @Test
    void criarEBuscarComDonoEAdminFuncionam() {
        Contexto ctx = criarContexto();
        assertNotNull(ctx.desafio());

        Authentication authAdmin = new UsernamePasswordAuthenticationToken(
                "admin@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication authInvalido = new UsernamePasswordAuthenticationToken(
                "outro@empresa.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));

        ResponseEntity<DesafioValidacaoResponseDTO> respAdmin =
                ctx.controller().buscar(ctx.desafio().id(), authAdmin);
        assertEquals(HttpStatus.OK, respAdmin.getStatusCode());
        assertEquals("Triagem Automatizada com IA", respAdmin.getBody().titulo());

        ResponseEntity<DesafioValidacaoResponseDTO> respInvalido =
                ctx.controller().buscar(ctx.desafio().id(), authInvalido);
        assertEquals(HttpStatus.FORBIDDEN, respInvalido.getStatusCode());
    }

    @Test
    void atualizarComAdminRetornaOk() {
        Contexto ctx = criarContexto();
        Authentication authAdmin = new UsernamePasswordAuthenticationToken(
                "admin@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        DesafioValidacaoUpdateDTO update = new DesafioValidacaoUpdateDTO(
                "Triagem Atualizada",
                "Problema revisado",
                "Pacientes geral",
                150,
                ModalidadeTeste.HIBRIDO,
                40,
                StatusDesafio.EM_ANDAMENTO
        );

        ResponseEntity<DesafioValidacaoResponseDTO> resp =
                ctx.controller().atualizar(ctx.desafio().id(), update, authAdmin);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Triagem Atualizada", resp.getBody().titulo());
        assertEquals(StatusDesafio.EM_ANDAMENTO, resp.getBody().status());
    }

    @Test
    void atualizarComUsuarioNaoAutorizadoRetorna403() {
        Contexto ctx = criarContexto();
        Authentication authInvalido = new UsernamePasswordAuthenticationToken(
                "outro@ambiente.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));

        DesafioValidacaoUpdateDTO update = new DesafioValidacaoUpdateDTO(
                "Invasão", null, null, null, null, null, null);

        ResponseEntity<DesafioValidacaoResponseDTO> resp =
                ctx.controller().atualizar(ctx.desafio().id(), update, authInvalido);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void patchInativarDesafioSoftDelete() {
        Contexto ctx = criarContexto();
        Authentication authAdmin = new UsernamePasswordAuthenticationToken(
                "admin@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        ResponseEntity<DesafioValidacaoResponseDTO> inativadoResp =
                ctx.controller().inativar(ctx.desafio().id(), authAdmin);

        assertEquals(HttpStatus.OK, inativadoResp.getStatusCode());
        assertFalse(inativadoResp.getBody().ativo());

        // Como foi inativado, não deve mais aparecer na listagem pública anônima
        ResponseEntity<List<DesafioAnonimoResponseDTO>> publicos = ctx.controller().listarAtivosAnonimos();
        assertTrue(publicos.getBody().isEmpty());
    }

    @Test
    void patchInativarComUsuarioNaoAutorizadoRetorna403() {
        Contexto ctx = criarContexto();
        Authentication authInvalido = new UsernamePasswordAuthenticationToken(
                "usuario@desconhecido.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));

        ResponseEntity<DesafioValidacaoResponseDTO> inativadoResp =
                ctx.controller().inativar(ctx.desafio().id(), authInvalido);
        assertEquals(HttpStatus.FORBIDDEN, inativadoResp.getStatusCode());
    }

    @Test
    void listarPorAmbienteComPermissoes() {
        Contexto ctx = criarContexto();
        UUID ambienteId = ctx.desafio().ambienteValidacaoId();

        Authentication authAdmin = new UsernamePasswordAuthenticationToken(
                "admin@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication authInvalido = new UsernamePasswordAuthenticationToken(
                "estranho@outro.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));

        ResponseEntity<List<DesafioValidacaoResponseDTO>> respAdmin =
                ctx.controller().listarPorAmbiente(ambienteId, authAdmin);
        assertEquals(HttpStatus.OK, respAdmin.getStatusCode());
        assertEquals(1, respAdmin.getBody().size());

        ResponseEntity<List<DesafioValidacaoResponseDTO>> respInvalido =
                ctx.controller().listarPorAmbiente(ambienteId, authInvalido);
        assertEquals(HttpStatus.FORBIDDEN, respInvalido.getStatusCode());

        ResponseEntity<List<DesafioValidacaoResponseDTO>> respVazia =
                ctx.controller().listarPorAmbiente(UUID.randomUUID(), authInvalido);
        assertEquals(HttpStatus.OK, respVazia.getStatusCode());
        assertTrue(respVazia.getBody().isEmpty());
    }

    @Test
    void operacoesSemAutenticacaoRetornam403() {
        Contexto ctx = criarContexto();

        assertEquals(HttpStatus.FORBIDDEN,
                ctx.controller().buscar(ctx.desafio().id(), null).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                ctx.controller().atualizar(ctx.desafio().id(), new DesafioValidacaoUpdateDTO(null, null, null, null, null, null, null), null).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                ctx.controller().inativar(ctx.desafio().id(), null).getStatusCode());
    }

    @Test
    void atualizarEDesativarComAmbienteProprietarioEAdmin() {
        Contexto ctx = criarContexto();
        Authentication authAdmin = new UsernamePasswordAuthenticationToken(
                "admin@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication authAmbienteProprietario = new UsernamePasswordAuthenticationToken(
                "contato@ambiente.com.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));

        ResponseEntity<DesafioValidacaoResponseDTO> buscaResp =
                ctx.controller().buscar(ctx.desafio().id(), authAdmin);
        assertEquals(HttpStatus.OK, buscaResp.getStatusCode());

        DesafioValidacaoUpdateDTO update = new DesafioValidacaoUpdateDTO("Novo Titulo", null, null, null, null, null, null);
        ResponseEntity<DesafioValidacaoResponseDTO> atualizadoResp =
                ctx.controller().atualizar(ctx.desafio().id(), update, authAmbienteProprietario);
        assertEquals(HttpStatus.OK, atualizadoResp.getStatusCode());
        assertEquals("Novo Titulo", atualizadoResp.getBody().titulo());
    }
}
