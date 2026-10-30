package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.MentorRequestDTO;
import br.senac.fullstack.dto.MentorResponseDTO;
import br.senac.fullstack.dto.MentorUpdateDTO;
import br.senac.fullstack.model.Mentor;
import br.senac.fullstack.repository.MentorRepository;
import br.senac.fullstack.service.AceiteTermoService;
import br.senac.fullstack.service.FixedObjectProvider;
import br.senac.fullstack.service.MentorService;
import br.senac.fullstack.service.NullObjectProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MentorControllerTest {

    private record Contexto(MentorController controller, MentorResponseDTO mentor) { }

    private Contexto criarContexto() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        var aceite = aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1");
        MentorService mentorService = new MentorService(new NullObjectProvider<>(), aceiteService);
        MentorController controller = new MentorController(mentorService);

        MentorRequestDTO req = new MentorRequestDTO(
                "Carlos Mentor", "carlos@senac.br", "senha123",
                List.of("Financas"), List.of("Pitch"), "10 anos",
                "Semanal", "SP", "11999999999", "https://linkedin.com/in/carlos", aceite.id());
        ResponseEntity<MentorResponseDTO> criado = controller.criar(req);
        return new Contexto(controller, criado.getBody());
    }

    @Test
    void criarBuscarEListarFuncionam() {
        Contexto ctx = criarContexto();
        assertNotNull(ctx.mentor());

        ResponseEntity<MentorResponseDTO> buscado = ctx.controller().buscar(ctx.mentor().id());
        assertEquals(HttpStatus.OK, buscado.getStatusCode());
        assertEquals("Carlos Mentor", buscado.getBody().nome());

        ResponseEntity<List<MentorResponseDTO>> lista = ctx.controller().listar();
        assertEquals(HttpStatus.OK, lista.getStatusCode());
        assertFalse(lista.getBody().isEmpty());
    }

    @Test
    void atualizarComRoleAdminRetornaOk() {
        Contexto ctx = criarContexto();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        MentorUpdateDTO update = new MentorUpdateDTO(
                "Carlos Admin Edit", List.of("IA"), List.of("SaaS"),
                "12 anos", "Quinzenal", "RJ", "11888888888", "https://linkedin.com/in/carlos2");

        ResponseEntity<MentorResponseDTO> resp = ctx.controller().atualizar(ctx.mentor().id(), update, auth);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Carlos Admin Edit", resp.getBody().nome());
    }

    @Test
    void atualizarComMesmoEmailDoMentorRetornaOk() {
        Contexto ctx = criarContexto();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "carlos@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_MENTOR")));
        MentorUpdateDTO update = new MentorUpdateDTO(
                "Carlos Proprio", null, null, null, null, null, null, null);

        ResponseEntity<MentorResponseDTO> resp = ctx.controller().atualizar(ctx.mentor().id(), update, auth);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Carlos Proprio", resp.getBody().nome());
    }

    @Test
    void atualizarComEmailDiferenteRetorna403() {
        Contexto ctx = criarContexto();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "outro@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_MENTOR")));
        MentorUpdateDTO update = new MentorUpdateDTO(
                "Invasor", null, null, null, null, null, null, null);

        ResponseEntity<MentorResponseDTO> resp = ctx.controller().atualizar(ctx.mentor().id(), update, auth);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void atualizarQuandoEmailDoMentorForNuloRetorna403() {
        MentorRepository repo = mock(MentorRepository.class);
        Mentor mentorSemEmail = new Mentor();
        UUID id = mentorSemEmail.getId();
        mentorSemEmail.setNome("Sem Email");
        mentorSemEmail.setEmail(null);
        when(repo.findById(id)).thenReturn(Optional.of(mentorSemEmail));

        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        MentorService mentorService = new MentorService(new FixedObjectProvider<>(repo), aceiteService);
        MentorController controller = new MentorController(mentorService);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "carlos@senac.br", "pass", List.of(new SimpleGrantedAuthority("ROLE_MENTOR")));
        MentorUpdateDTO update = new MentorUpdateDTO(
                "Tentativa", null, null, null, null, null, null, null);

        ResponseEntity<MentorResponseDTO> resp = controller.atualizar(id, update, auth);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }
}
