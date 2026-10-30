package br.senac.fullstack.controller;

import br.senac.fullstack.dto.VinculoResponseDTO;
import br.senac.fullstack.model.StatusVinculo;
import br.senac.fullstack.service.AceiteTermoService;
import br.senac.fullstack.service.MentorService;
import br.senac.fullstack.service.NullObjectProvider;
import br.senac.fullstack.service.VinculoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VinculoControllerTest {

    @Test
    void buscarVinculoPorStartupRetornaOk() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        MentorService mentorService = new MentorService(new NullObjectProvider<>(), aceiteService);
        VinculoService vinculoService = new VinculoService(new NullObjectProvider<>(), mentorService);
        VinculoController controller = new VinculoController(vinculoService);

        UUID startupId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        vinculoService.criar(startupId, mentorId);

        ResponseEntity<VinculoResponseDTO> resp = controller.buscar(startupId);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(StatusVinculo.ATIVO, resp.getBody().status());
    }
}
