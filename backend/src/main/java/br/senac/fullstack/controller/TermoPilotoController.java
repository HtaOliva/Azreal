package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.ConfirmacaoExecucaoRequestDTO;
import br.senac.fullstack.dto.ConfirmacaoExecucaoResponseDTO;
import br.senac.fullstack.dto.EntregaPilotoRequestDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.service.TermoPilotoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/{id}/registrar-entrega")
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<TermoPilotoResponseDTO> registrarEntrega(
            @PathVariable("id") UUID id,
            @Valid @RequestBody EntregaPilotoRequestDTO request,
            Authentication authentication) {
        TermoPiloto termo = termoPilotoService.registrarEntrega(id, request, authentication);
        AmbienteValidacaoResponseDTO ambienteRevelado =
                termoPilotoService.buscarAmbienteRevelado(termo.getAmbienteValidacaoId());
        return ResponseEntity.ok(TermoPilotoResponseDTO.fromEntity(termo, termo.getStatus(), ambienteRevelado));
    }

    @PostMapping("/{id}/confirmar-execucao")
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<ConfirmacaoExecucaoResponseDTO> confirmarExecucao(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ConfirmacaoExecucaoRequestDTO request,
            Authentication authentication) {
        TermoPiloto termo = termoPilotoService.confirmarExecucao(id, request, authentication);
        return ResponseEntity.ok(ConfirmacaoExecucaoResponseDTO.fromEntity(termo));
    }
}
