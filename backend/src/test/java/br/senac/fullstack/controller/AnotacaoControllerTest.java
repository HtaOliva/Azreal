package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AnotacaoRequestDTO;
import br.senac.fullstack.dto.AnotacaoResponseDTO;
import br.senac.fullstack.service.AceiteTermoService;
import br.senac.fullstack.service.AnotacaoService;
import br.senac.fullstack.service.MentorService;
import br.senac.fullstack.service.NullObjectProvider;
import br.senac.fullstack.service.VinculoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnotacaoControllerTest {

    @Test
    void criarAnotacaoRetornaCreated() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        MentorService mentorService = new MentorService(new NullObjectProvider<>(), aceiteService);
        VinculoService vinculoService = new VinculoService(new NullObjectProvider<>(), mentorService);
        AnotacaoService anotacaoService = new AnotacaoService(new NullObjectProvider<>(), vinculoService);
        AnotacaoController controller = new AnotacaoController(anotacaoService);

        UUID startupId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        vinculoService.criar(startupId, mentorId);

        AnotacaoRequestDTO request = new AnotacaoRequestDTO(mentorId, UUID.randomUUID(), "Conteudo da anotacao");
        ResponseEntity<AnotacaoResponseDTO> response = controller.criar(startupId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(startupId, response.getBody().startupId());
        assertEquals(mentorId, response.getBody().mentorId());
        assertEquals("Conteudo da anotacao", response.getBody().conteudo());
    }
}
