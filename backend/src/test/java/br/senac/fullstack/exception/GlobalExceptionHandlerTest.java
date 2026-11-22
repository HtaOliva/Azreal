package br.senac.fullstack.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException retornando status 400 e invalidParams")
    void deveTratarValidationException() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objetoTeste");
        bindingResult.addError(new FieldError("objetoTeste", "titulo", "nao deve estar em branco"));

        MethodParameter parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("deveTratarValidationException"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ProblemDetail> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Erro de Validacao de Dados", response.getBody().getTitle());

        @SuppressWarnings("unchecked")
        Map<String, String> invalidParams = (Map<String, String>) response.getBody().getProperties().get("invalidParams");
        assertNotNull(invalidParams);
        assertEquals("nao deve estar em branco", invalidParams.get("titulo"));
    }

    @Test
    @DisplayName("Deve tratar ConstraintViolationException")
    void deveTratarConstraintViolation() {
        Set<ConstraintViolation<?>> violations = Collections.emptySet();
        ConstraintViolationException ex = new ConstraintViolationException("Restricao violada", violations);

        ResponseEntity<ProblemDetail> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Violacao de Restricao", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("Restricao violada"));
    }

    @Test
    @DisplayName("Deve tratar ConstraintViolationException com mensagem nula")
    void deveTratarConstraintViolationMensagemNula() {
        ConstraintViolationException ex = new ConstraintViolationException(null, Collections.emptySet());

        ResponseEntity<ProblemDetail> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Restricao de validacao violada.", response.getBody().getDetail());
    }

    @Test
    @DisplayName("Deve tratar ResponseStatusException")
    void deveTratarResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso nao encontrado");

        ResponseEntity<ProblemDetail> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Recurso nao encontrado", response.getBody().getDetail());
    }

    @Test
    @DisplayName("Deve tratar ResponseStatusException com razao nula")
    void deveTratarResponseStatusExceptionRazaoNula() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT);

        ResponseEntity<ProblemDetail> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Erro na requisicao.", response.getBody().getDetail());
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException")
    void deveTratarIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Parametro invalido");

        ResponseEntity<ProblemDetail> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Parametro invalido", response.getBody().getDetail());
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException com mensagem nula")
    void deveTratarIllegalArgumentExceptionMensagemNula() {
        IllegalArgumentException ex = new IllegalArgumentException((String) null);

        ResponseEntity<ProblemDetail> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Argumento invalido fornecido.", response.getBody().getDetail());
    }

    @Test
    @DisplayName("Deve tratar IllegalStateException")
    void deveTratarIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Conflito de estado");

        ResponseEntity<ProblemDetail> response = handler.handleIllegalStateException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conflito de estado", response.getBody().getDetail());
    }

    @Test
    @DisplayName("Deve tratar IllegalStateException com mensagem nula")
    void deveTratarIllegalStateExceptionMensagemNula() {
        IllegalStateException ex = new IllegalStateException((String) null);

        ResponseEntity<ProblemDetail> response = handler.handleIllegalStateException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Estado invalido para a operacao solicitada.", response.getBody().getDetail());
    }

    @Test
    @DisplayName("Deve tratar AiMatchmakingException")
    void deveTratarAiMatchmakingException() {
        AiMatchmakingException ex = new AiMatchmakingException("Gemini fora do ar");

        ResponseEntity<ProblemDetail> response = handler.handleAiMatchmakingException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Gemini fora do ar", response.getBody().getDetail());
    }

    @Test
    @DisplayName("Deve tratar AiMatchmakingException com mensagem nula")
    void deveTratarAiMatchmakingExceptionMensagemNula() {
        AiMatchmakingException ex = new AiMatchmakingException(null);

        ResponseEntity<ProblemDetail> response = handler.handleAiMatchmakingException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Falha na integracao com servico de IA de Matchmaking.", response.getBody().getDetail());
    }

    @Test
    @DisplayName("Deve tratar BadCredentialsException")
    void deveTratarBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Senha incorreta");

        ResponseEntity<ProblemDetail> response = handler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Nao Autorizado", response.getBody().getTitle());
    }

    @Test
    @DisplayName("Deve tratar AccessDeniedException para usuario autenticado com 403 Forbidden")
    void deveTratarAccessDeniedException() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user", "pass", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            AccessDeniedException ex = new AccessDeniedException("Sem permissao");

            ResponseEntity<ProblemDetail> response = handler.handleAccessDenied(ex);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Acesso Proibido", response.getBody().getTitle());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized para usuario anonimo/nao autenticado")
    void deveRetornar401ParaAnonimo() {
        SecurityContextHolder.clearContext();
        AccessDeniedException ex = new AccessDeniedException("Anonimo");
        ResponseEntity<ProblemDetail> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Nao Autorizado", response.getBody().getTitle());
    }

    @Test
    @DisplayName("Deve tratar Generic Exception com 500")
    void deveTratarGenericException() {
        Exception ex = new RuntimeException("Falha inesperada");

        ResponseEntity<ProblemDetail> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Erro Interno do Servidor", response.getBody().getTitle());
    }
}
