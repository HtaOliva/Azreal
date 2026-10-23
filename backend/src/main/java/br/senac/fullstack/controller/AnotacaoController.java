package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AnotacaoRequestDTO;
import br.senac.fullstack.dto.AnotacaoResponseDTO;
import br.senac.fullstack.service.AnotacaoService;
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
public class AnotacaoController {

    private final AnotacaoService anotacaoService;

    public AnotacaoController(AnotacaoService anotacaoService) {
        this.anotacaoService = anotacaoService;
    }

    @PostMapping("/{id}/anotacoes")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<AnotacaoResponseDTO> criar(@PathVariable("id") UUID id,
                                                     @Valid @RequestBody AnotacaoRequestDTO request) {
        AnotacaoRequestDTO fixed = new AnotacaoRequestDTO(request.mentorId(), id, request.conteudo());
        return ResponseEntity.status(HttpStatus.CREATED).body(anotacaoService.criar(fixed));
    }
}
