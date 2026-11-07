package br.senac.fullstack.controller;

import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.service.TermoPilotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/termos-piloto")
public class TermoPilotoController {

    private final TermoPilotoService termoPilotoService;

    @Autowired
    public TermoPilotoController(TermoPilotoService termoPilotoService) {
        this.termoPilotoService = termoPilotoService;
    }

    @GetMapping("/{propostaId}")
    @PreAuthorize("hasRole('STARTUP') or hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<TermoPilotoResponseDTO> buscarPorProposta(
            @PathVariable("propostaId") UUID propostaId,
            Authentication authentication) {
        return ResponseEntity.ok(termoPilotoService.obterTermoComIdentidadeRevelada(propostaId, authentication));
    }
}
