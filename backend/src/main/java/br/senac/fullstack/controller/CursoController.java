package br.senac.fullstack.controller;

import br.senac.fullstack.dto.CursoRequestDTO;
import br.senac.fullstack.dto.CursoResponseDTO;
import br.senac.fullstack.service.CursoService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public ResponseEntity<List<CursoResponseDTO>> listarAtivos() {
        return ResponseEntity.ok(cursoService.listarAtivos());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CursoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(cursoService.listarTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoResponseDTO> criar(@Valid @RequestBody CursoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoResponseDTO> atualizar(@PathVariable("id") UUID id,
                                                     @Valid @RequestBody CursoRequestDTO request) {
        return ResponseEntity.ok(cursoService.atualizar(id, request));
    }

    @PutMapping("/{id}/ativo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> definirAtivo(@PathVariable("id") UUID id,
                                            @org.springframework.web.bind.annotation.RequestParam boolean ativo) {
        cursoService.definirAtivo(id, ativo);
        return ResponseEntity.noContent().build();
    }
}
