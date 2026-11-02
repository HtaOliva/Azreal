package br.senac.fullstack.controller;

import br.senac.fullstack.dto.IncubadoraRequestDTO;
import br.senac.fullstack.dto.IncubadoraResponseDTO;
import br.senac.fullstack.service.IncubadoraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incubadoras")
public class IncubadoraController {

    private final IncubadoraService incubadoraService;

    public IncubadoraController(IncubadoraService incubadoraService) {
        this.incubadoraService = incubadoraService;
    }

    @GetMapping
    public ResponseEntity<List<IncubadoraResponseDTO>> listarAtivas() {
        return ResponseEntity.ok(incubadoraService.listarAtivas());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IncubadoraResponseDTO>> listarTodos() {
        return ResponseEntity.ok(incubadoraService.listarTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncubadoraResponseDTO> criar(@Valid @RequestBody IncubadoraRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incubadoraService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncubadoraResponseDTO> atualizar(@PathVariable("id") UUID id,
                                                          @Valid @RequestBody IncubadoraRequestDTO request) {
        return ResponseEntity.ok(incubadoraService.atualizar(id, request));
    }

    @PutMapping("/{id}/ativo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> definirAtivo(@PathVariable("id") UUID id, @RequestParam boolean ativo) {
        incubadoraService.definirAtivo(id, ativo);
        return ResponseEntity.noContent().build();
    }
}
