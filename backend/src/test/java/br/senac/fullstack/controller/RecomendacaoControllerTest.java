package br.senac.fullstack.controller;

import br.senac.fullstack.dto.RecomendacaoRequestDTO;
import br.senac.fullstack.dto.RecomendacaoResponseDTO;
import br.senac.fullstack.service.AceiteTermoService;
import br.senac.fullstack.service.MentorService;
import br.senac.fullstack.service.NullObjectProvider;
import br.senac.fullstack.service.RecomendacaoService;
import br.senac.fullstack.service.VinculoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RecomendacaoControllerTest {

    @Test
    void criarRecomendacaoRetornaCreated() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        MentorService mentorService = new MentorService(new NullObjectProvider<>(), aceiteService);
        VinculoService vinculoService = new VinculoService(new NullObjectProvider<>(), mentorService);
        RecomendacaoService recomendacaoService = new RecomendacaoService(new NullObjectProvider<>(), vinculoService);
        RecomendacaoController controller = new RecomendacaoController(recomendacaoService);

        UUID startupId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        vinculoService.criar(startupId, mentorId);

        RecomendacaoRequestDTO req = new RecomendacaoRequestDTO(
                mentorId, startupId, "INCUBADORA", "Excelente potencial");
        ResponseEntity<RecomendacaoResponseDTO> resp = controller.criar(req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("INCUBADORA", resp.getBody().tipoApoio());
    }
}
