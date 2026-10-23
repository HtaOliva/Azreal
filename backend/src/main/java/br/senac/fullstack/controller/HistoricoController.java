package br.senac.fullstack.controller;

import br.senac.fullstack.dto.HistoricoResponseDTO;
import br.senac.fullstack.service.HistoricoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/startups")
public class HistoricoController {

    private final HistoricoService historicoService;

    public HistoricoController(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasRole('STARTUP') or hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<HistoricoResponseDTO> buscar(@PathVariable("id") UUID id,
                                                      Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        historicoService.verificarAcesso(id, authentication.getName(), isAdmin);
        return ResponseEntity.ok(historicoService.buscar(id));
    }
}
