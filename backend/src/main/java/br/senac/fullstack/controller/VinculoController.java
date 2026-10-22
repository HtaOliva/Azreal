package br.senac.fullstack.controller;

import br.senac.fullstack.dto.VinculoResponseDTO;
import br.senac.fullstack.service.VinculoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/startups")
public class VinculoController {

    private final VinculoService vinculoService;

    public VinculoController(VinculoService vinculoService) {
        this.vinculoService = vinculoService;
    }

    @GetMapping("/{id}/vinculo")
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<VinculoResponseDTO> buscar(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(vinculoService.buscarPorStartup(id));
    }
}
