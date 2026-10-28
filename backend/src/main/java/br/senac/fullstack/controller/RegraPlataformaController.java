package br.senac.fullstack.controller;

import br.senac.fullstack.dto.RegraPlataformaRequestDTO;
import br.senac.fullstack.dto.RegraPlataformaResponseDTO;
import br.senac.fullstack.service.RegraPlataformaService;
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
@RequestMapping("/regras")
public class RegraPlataformaController {

    private final RegraPlataformaService regraService;

    public RegraPlataformaController(RegraPlataformaService regraService) {
        this.regraService = regraService;
    }

    @GetMapping
    public ResponseEntity<RegraPlataformaResponseDTO> buscarAtiva() {
        return ResponseEntity.ok(regraService.buscarAtiva());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RegraPlataformaResponseDTO>> listar() {
        return ResponseEntity.ok(regraService.listar());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegraPlataformaResponseDTO> criar(@Valid @RequestBody RegraPlataformaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(regraService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegraPlataformaResponseDTO> atualizar(@PathVariable("id") UUID id,
                                                               @Valid @RequestBody RegraPlataformaRequestDTO request) {
        return ResponseEntity.ok(regraService.atualizar(id, request));
    }

    @PutMapping("/{id}/ativo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> definirAtivo(@PathVariable("id") UUID id, @RequestParam boolean ativo) {
        regraService.definirAtivo(id, ativo);
        return ResponseEntity.noContent().build();
    }
}