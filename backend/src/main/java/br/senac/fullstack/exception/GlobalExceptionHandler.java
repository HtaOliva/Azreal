package br.senac.fullstack.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP_PROPERTY = "timestamp";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos contem valores invalidos. Corrija e tente novamente."
        );
        problem.setTitle("Erro de Validacao de Dados");
        problem.setType(URI.create("urn:problem-type:validation-error"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        Map<String, String> invalidFields = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            invalidFields.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("invalidParams", invalidFields);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : "Restricao de validacao violada."
        );
        problem.setTitle("Violacao de Restricao");
        problem.setType(URI.create("urn:problem-type:constraint-violation"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatusException(ResponseStatusException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                ex.getStatusCode(),
                ex.getReason() != null ? ex.getReason() : "Erro na requisicao."
        );
        problem.setTitle("Erro de Execucao");
        problem.setType(URI.create("urn:problem-type:response-status"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return ResponseEntity.status(ex.getStatusCode()).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : "Argumento invalido fornecido."
        );
        problem.setTitle("Argumento Invalido");
        problem.setType(URI.create("urn:problem-type:illegal-argument"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalStateException(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage() != null ? ex.getMessage() : "Estado invalido para a operacao solicitada."
        );
        problem.setTitle("Conflito de Estado de Negocio");
        problem.setType(URI.create("urn:problem-type:illegal-state"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(AiMatchmakingException.class)
    public ResponseEntity<ProblemDetail> handleAiMatchmakingException(AiMatchmakingException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                ex.getMessage() != null ? ex.getMessage() : "Falha na integracao com servico de IA de Matchmaking."
        );
        problem.setTitle("Falha no Matchmaking Inteligente");
        problem.setType(URI.create("urn:problem-type:ai-matchmaking"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Credenciais de acesso invalidas. Verifique seu usuario e senha."
        );
        problem.setTitle("Nao Autorizado");
        problem.setType(URI.create("urn:problem-type:unauthorized"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Acesso nao autorizado. E necessario autenticar-se para acessar este recurso."
            );
            problem.setTitle("Nao Autorizado");
            problem.setType(URI.create("urn:problem-type:unauthorized"));
            problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Acesso negado: seu perfil de usuario nao possui permissao para este recurso."
        );
        problem.setTitle("Acesso Proibido");
        problem.setType(URI.create("urn:problem-type:forbidden"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado no servidor. Contate os administradores."
        );
        problem.setTitle("Erro Interno do Servidor");
        problem.setType(URI.create("urn:problem-type:internal-server-error"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
