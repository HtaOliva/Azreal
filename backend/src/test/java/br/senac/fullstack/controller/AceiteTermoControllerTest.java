package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.AceiteTermoResponseDTO;
import br.senac.fullstack.service.AceiteTermoService;
import br.senac.fullstack.service.NullObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AceiteTermoControllerTest {

    @Test
    void registrarComIpNoRequestUsaIpDoRequest() {
        AceiteTermoService service = new AceiteTermoService(new NullObjectProvider<>());
        AceiteTermoController controller = new AceiteTermoController(service);
        HttpServletRequest httpRequest = new MockHttpServletRequest();

        AceiteTermoRequestDTO request = new AceiteTermoRequestDTO("v1.0", "192.168.1.10");
        ResponseEntity<AceiteTermoResponseDTO> response = controller.registrar(request, httpRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("v1.0", response.getBody().versaoTermo());
    }

    @Test
    void registrarComIpNuloUsaRemoteAddr() {
        AceiteTermoService service = new AceiteTermoService(new NullObjectProvider<>());
        AceiteTermoController controller = new AceiteTermoController(service);
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("127.0.0.1");

        AceiteTermoRequestDTO request = new AceiteTermoRequestDTO("v1.1", null);
        ResponseEntity<AceiteTermoResponseDTO> response = controller.registrar(request, httpRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("v1.1", response.getBody().versaoTermo());
    }

    @Test
    void registrarComIpEmBrancoUsaRemoteAddr() {
        AceiteTermoService service = new AceiteTermoService(new NullObjectProvider<>());
        AceiteTermoController controller = new AceiteTermoController(service);
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("10.0.0.5");

        AceiteTermoRequestDTO request = new AceiteTermoRequestDTO("v1.2", "   ");
        ResponseEntity<AceiteTermoResponseDTO> response = controller.registrar(request, httpRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("v1.2", response.getBody().versaoTermo());
    }
}
