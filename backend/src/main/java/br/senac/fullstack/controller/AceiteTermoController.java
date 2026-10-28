package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.AceiteTermoResponseDTO;
import br.senac.fullstack.service.AceiteTermoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/termos")
public class AceiteTermoController {

    private final AceiteTermoService aceiteTermoService;

    public AceiteTermoController(AceiteTermoService aceiteTermoService) {
        this.aceiteTermoService = aceiteTermoService;
    }

    @PostMapping("/aceite")
    public ResponseEntity<AceiteTermoResponseDTO> registrar(@Valid @RequestBody AceiteTermoRequestDTO request,
                                                          HttpServletRequest httpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID usuarioId = null;
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            // LLDAP principal name is not a UUID; keep null for anonymous/public cadastro
        }
        String ip = httpRequest.getRemoteAddr();
        if (request.ip() != null && !request.ip().isBlank()) {
            ip = request.ip();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aceiteTermoService.registrar(request, usuarioId, ip));
    }
}
