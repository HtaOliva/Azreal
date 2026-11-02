package br.senac.fullstack.controller;

import br.senac.fullstack.dto.TemaRequestDTO;
import br.senac.fullstack.dto.TemaResponseDTO;
import br.senac.fullstack.service.NullObjectProvider;
import br.senac.fullstack.service.TemaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TemaControllerTest {

    @Test
    void fluxoCompletoTemaController() {
        TemaService service = new TemaService(new NullObjectProvider<>());
        TemaController controller = new TemaController(service);

        TemaRequestDTO req = new TemaRequestDTO("Inteligencia Artificial", "Modelos generativos", true);
        ResponseEntity<TemaResponseDTO> criado = controller.criar(req);
        assertEquals(HttpStatus.CREATED, criado.getStatusCode());
        assertNotNull(criado.getBody());

        ResponseEntity<List<TemaResponseDTO>> ativos = controller.listarAtivos();
        assertEquals(HttpStatus.OK, ativos.getStatusCode());
        assertFalse(ativos.getBody().isEmpty());

        ResponseEntity<List<TemaResponseDTO>> todos = controller.listarTodos();
        assertEquals(HttpStatus.OK, todos.getStatusCode());
        assertFalse(todos.getBody().isEmpty());

        TemaRequestDTO updateReq = new TemaRequestDTO("IA Aplicada", "Nova descricao", true);
        ResponseEntity<TemaResponseDTO> atualizado = controller.atualizar(criado.getBody().id(), updateReq);
        assertEquals(HttpStatus.OK, atualizado.getStatusCode());
        assertEquals("IA Aplicada", atualizado.getBody().nome());

        ResponseEntity<Void> inativado = controller.definirAtivo(criado.getBody().id(), false);
        assertEquals(HttpStatus.NO_CONTENT, inativado.getStatusCode());
    }
}
