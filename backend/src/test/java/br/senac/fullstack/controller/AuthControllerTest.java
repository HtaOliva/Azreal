package br.senac.fullstack.controller;

import br.senac.fullstack.dto.LoginRequestDTO;
import br.senac.fullstack.dto.LoginResponseDTO;
import br.senac.fullstack.dto.MeResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthControllerTest {

    private AuthController createController() {
        AuthenticationManager authManager = authentication -> new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        return new AuthController(authManager);
    }

    @Test
    void loginRetornaTokenERoles() {
        AuthController controller = createController();
        LoginRequestDTO request = new LoginRequestDTO("admin", "senha123");

        ResponseEntity<LoginResponseDTO> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mock-jwt-token", response.getBody().token());
        assertEquals(List.of("ROLE_ADMIN"), response.getBody().roles());
    }

    @Test
    void meSemAutenticacaoRetorna401() {
        AuthController controller = createController();

        ResponseEntity<MeResponseDTO> response = controller.me(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@senac.br, ROLE_ADMIN, ADMIN",
            "mentor@senac.br, ROLE_MENTOR, MENTOR",
            "startup@senac.br, ROLE_STARTUP, STARTUP",
            "visitante@senac.br, ROLE_GUEST, NONE"
    })
    void meComAutenticacaoRetornaPerfilEsperado(String email, String role, String perfilEsperado) {
        AuthController controller = createController();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                email, "pass", List.of(new SimpleGrantedAuthority(role)));

        ResponseEntity<MeResponseDTO> response = controller.me(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(perfilEsperado, response.getBody().perfil());
    }
}
