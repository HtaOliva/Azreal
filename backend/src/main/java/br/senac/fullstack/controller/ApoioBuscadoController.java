package br.senac.fullstack.controller;

import br.senac.fullstack.dto.ApoioBuscadoRequestDTO;
import br.senac.fullstack.dto.ApoioBuscadoResponseDTO;
import br.senac.fullstack.service.ApoioBuscadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/startups")
public class ApoioBuscadoController {

    private final ApoioBuscadoService apoioBuscadoService;

    public ApoioBuscadoController(ApoioBuscadoService apoioBuscadoService) {
        this.apoioBuscadoService = apoioBuscadoService;
    }

    @PostMapping("/{id}/apoios-buscados")
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<ApoioBuscadoResponseDTO> registrar(@PathVariable("id") UUID id,
                                                            @Valid @RequestBody ApoioBuscadoRequestDTO request) {
        // garante que o path id corresponde ao body
        ApoioBuscadoRequestDTO fixed = new ApoioBuscadoRequestDTO(id, request.tipoApoio());
        return ResponseEntity.status(HttpStatus.CREATED).body(apoioBuscadoService.registrar(fixed));
    }
}
