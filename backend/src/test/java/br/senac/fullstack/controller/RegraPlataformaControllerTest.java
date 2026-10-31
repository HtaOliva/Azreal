package br.senac.fullstack.controller;

import br.senac.fullstack.dto.RegraPlataformaRequestDTO;
import br.senac.fullstack.dto.RegraPlataformaResponseDTO;
import br.senac.fullstack.service.NullObjectProvider;
import br.senac.fullstack.service.RegraPlataformaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegraPlataformaControllerTest {

    @Test
    void fluxoCompletoRegraPlataformaController() {
        RegraPlataformaService service = new RegraPlataformaService(new NullObjectProvider<>());
        RegraPlataformaController controller = new RegraPlataformaController(service);

        RegraPlataformaRequestDTO req = new RegraPlataformaRequestDTO("Regra inicial", true);
        ResponseEntity<RegraPlataformaResponseDTO> criada = controller.criar(req);
        assertEquals(HttpStatus.CREATED, criada.getStatusCode());
        assertNotNull(criada.getBody());

        ResponseEntity<RegraPlataformaResponseDTO> ativa = controller.buscarAtiva();
        assertEquals(HttpStatus.OK, ativa.getStatusCode());
        assertEquals("Regra inicial", ativa.getBody().texto());

        ResponseEntity<List<RegraPlataformaResponseDTO>> lista = controller.listar();
        assertEquals(HttpStatus.OK, lista.getStatusCode());
        assertFalse(lista.getBody().isEmpty());

        RegraPlataformaRequestDTO updateReq = new RegraPlataformaRequestDTO("Regra atualizada", true);
        ResponseEntity<RegraPlataformaResponseDTO> atualizada = controller.atualizar(criada.getBody().id(), updateReq);
        assertEquals(HttpStatus.OK, atualizada.getStatusCode());
        assertEquals("Regra atualizada", atualizada.getBody().texto());

        ResponseEntity<Void> inativada = controller.definirAtivo(criada.getBody().id(), false);
        assertEquals(HttpStatus.NO_CONTENT, inativada.getStatusCode());
    }
}
