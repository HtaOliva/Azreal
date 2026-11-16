package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.AvaliacaoMatchRequestDTO;
import br.senac.fullstack.dto.AvaliacaoMatchResponseDTO;
import br.senac.fullstack.dto.ConfirmacaoExecucaoRequestDTO;
import br.senac.fullstack.dto.ConfirmacaoExecucaoResponseDTO;
import br.senac.fullstack.dto.EntregaPilotoRequestDTO;
import br.senac.fullstack.dto.RegistroAuditoriaResponseDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.service.AuditoriaService;
import br.senac.fullstack.service.AvaliacaoMatchService;
import br.senac.fullstack.service.TermoPilotoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/termos-piloto")
public class TermoPilotoController {

    private final TermoPilotoService termoPilotoService;
    private final ObjectProvider<AvaliacaoMatchService> avaliacaoMatchServiceProvider;
    private final ObjectProvider<AuditoriaService> auditoriaServiceProvider;

    @Autowired
    public TermoPilotoController(
            TermoPilotoService termoPilotoService,
            ObjectProvider<AvaliacaoMatchService> avaliacaoMatchServiceProvider,
            ObjectProvider<AuditoriaService> auditoriaServiceProvider) {
        this.termoPilotoService = termoPilotoService;
        this.avaliacaoMatchServiceProvider = avaliacaoMatchServiceProvider;
        this.auditoriaServiceProvider = auditoriaServiceProvider;
    }

    public TermoPilotoController(TermoPilotoService termoPilotoService) {
        this(termoPilotoService, null, null);
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

    @PostMapping(path = {"/{id}/avaliar-match", "/{id}/avaliacao-match"})
    @PreAuthorize("hasRole('STARTUP') or hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<AvaliacaoMatchResponseDTO> avaliarMatch(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AvaliacaoMatchRequestDTO request,
            Authentication authentication) {
        AvaliacaoMatchService matchService = avaliacaoMatchServiceProvider != null
                ? avaliacaoMatchServiceProvider.getIfAvailable() : null;
        if (matchService == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        AvaliacaoMatchResponseDTO response = matchService.avaliar(id, request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(path = {"/{id}/avaliar-match", "/{id}/avaliacoes-match", "/{id}/avaliacao-match"})
    @PreAuthorize("hasRole('STARTUP') or hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<List<AvaliacaoMatchResponseDTO>> listarAvaliacoesMatch(@PathVariable("id") UUID id) {
        AvaliacaoMatchService matchService = avaliacaoMatchServiceProvider != null
                ? avaliacaoMatchServiceProvider.getIfAvailable() : null;
        if (matchService == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(matchService.listarPorPropostaOuTermo(id));
    }

    @GetMapping("/{id}/auditoria")
    @PreAuthorize("hasRole('STARTUP') or hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<List<RegistroAuditoriaResponseDTO>> consultarAuditoria(@PathVariable("id") UUID id) {
        AuditoriaService auditoriaService = auditoriaServiceProvider != null
                ? auditoriaServiceProvider.getIfAvailable() : null;
        if (auditoriaService == null) {
            return ResponseEntity.ok(List.of());
        }
        List<RegistroAuditoriaResponseDTO> audit = auditoriaService.listarPorTermo(id);
        if (audit.isEmpty()) {
            audit = auditoriaService.listarPorProposta(id);
        }
        return ResponseEntity.ok(audit);
    }
}
