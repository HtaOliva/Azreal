package br.senac.fullstack.controller;

import br.senac.fullstack.model.VinculoMentoria;
import br.senac.fullstack.service.AceiteTermoService;
import br.senac.fullstack.service.MentorService;
import br.senac.fullstack.service.NullObjectProvider;
import br.senac.fullstack.service.VinculoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VinculoGestaoControllerTest {

    @Test
    void encerrarVinculoRetornaNoContent() {
        AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());
        MentorService mentorService = new MentorService(new NullObjectProvider<>(), aceiteService);
        VinculoService vinculoService = new VinculoService(new NullObjectProvider<>(), mentorService);
        VinculoGestaoController controller = new VinculoGestaoController(vinculoService);

        VinculoMentoria criado = vinculoService.criar(UUID.randomUUID(), UUID.randomUUID());

        ResponseEntity<Void> resp = controller.encerrar(criado.getId());
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }
}
