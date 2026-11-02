package br.senac.fullstack.controller;

import br.senac.fullstack.dto.TemaRequestDTO;
import br.senac.fullstack.dto.TemaResponseDTO;
import br.senac.fullstack.service.TemaService;
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
@RequestMapping("/temas")
public class TemaController {

    private final TemaService temaService;

    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }

    @GetMapping
    public ResponseEntity<List<TemaResponseDTO>> listarAtivos() {
        return ResponseEntity.ok(temaService.listarAtivos());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TemaResponseDTO>> listarTodos() {
        return ResponseEntity.ok(temaService.listarTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TemaResponseDTO> criar(@Valid @RequestBody TemaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(temaService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TemaResponseDTO> atualizar(@PathVariable("id") UUID id,
                                                     @Valid @RequestBody TemaRequestDTO request) {
        return ResponseEntity.ok(temaService.atualizar(id, request));
    }

    @PutMapping("/{id}/ativo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> definirAtivo(@PathVariable("id") UUID id, @RequestParam boolean ativo) {
        temaService.definirAtivo(id, ativo);
        return ResponseEntity.noContent().build();
    }
}