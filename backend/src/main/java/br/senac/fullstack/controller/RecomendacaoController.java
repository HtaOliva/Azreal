package br.senac.fullstack.controller;

import br.senac.fullstack.dto.RecomendacaoRequestDTO;
import br.senac.fullstack.dto.RecomendacaoResponseDTO;
import br.senac.fullstack.service.RecomendacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recomendacoes")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    public RecomendacaoController(RecomendacaoService recomendacaoService) {
        this.recomendacaoService = recomendacaoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<RecomendacaoResponseDTO> criar(@Valid @RequestBody RecomendacaoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recomendacaoService.criar(request));
    }
}
