package br.senac.fullstack.controller;

import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.StartupUpdateDTO;
import br.senac.fullstack.service.StartupService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/startups")
public class StartupController {

    private final StartupService startupService;

    public StartupController(StartupService startupService) {
        this.startupService = startupService;
    }

    @PostMapping
    public ResponseEntity<StartupResponseDTO> criar(@Valid @RequestBody StartupRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(startupService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StartupResponseDTO> buscar(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(startupService.buscar(id));
    }

    @GetMapping
    public ResponseEntity<List<StartupResponseDTO>> listar() {
        return ResponseEntity.ok(startupService.listar());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<StartupResponseDTO> atualizar(@PathVariable("id") UUID id,
                                                       @Valid @RequestBody StartupUpdateDTO request) {
        return ResponseEntity.ok(startupService.atualizar(id, request));
    }
}
