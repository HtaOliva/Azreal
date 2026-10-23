package br.senac.fullstack.controller;

import br.senac.fullstack.dto.VinculoResponseDTO;
import br.senac.fullstack.service.VinculoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/vinculos")
public class VinculoGestaoController {

    private final VinculoService vinculoService;

    public VinculoGestaoController(VinculoService vinculoService) {
        this.vinculoService = vinculoService;
    }

    @PutMapping("/{id}/encerrar")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> encerrar(@PathVariable("id") UUID id) {
        vinculoService.encerrar(id);
        return ResponseEntity.noContent().build();
    }
}
