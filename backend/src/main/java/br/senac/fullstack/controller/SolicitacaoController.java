package br.senac.fullstack.controller;

import br.senac.fullstack.dto.FilaItemResponseDTO;
import br.senac.fullstack.dto.RespostaPedidoRequestDTO;
import br.senac.fullstack.dto.SolicitacaoRequestDTO;
import br.senac.fullstack.dto.SolicitacaoResponseDTO;
import br.senac.fullstack.service.MentorService;
import br.senac.fullstack.service.SolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;
    private final MentorService mentorService;

    public SolicitacaoController(SolicitacaoService solicitacaoService, MentorService mentorService) {
        this.solicitacaoService = solicitacaoService;
        this.mentorService = mentorService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<SolicitacaoResponseDTO> criar(@Valid @RequestBody SolicitacaoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoService.criar(request));
    }

    @PutMapping("/{id}/responder")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<SolicitacaoResponseDTO> responder(@PathVariable("id") UUID id,
                                                            @Valid @RequestBody RespostaPedidoRequestDTO request,
                                                            Authentication authentication) {
        var mentor = mentorService.buscarEntidadePorEmail(authentication.getName());
        mentorService.exigirPerfilCompleto(mentor.getId());
        return ResponseEntity.ok(solicitacaoService.responder(id, mentor.getId(), request));
    }

    @GetMapping("/mentor/{mentorId}/fila")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<FilaItemResponseDTO>> fila(@PathVariable("mentorId") UUID mentorId) {
        mentorService.exigirPerfilCompleto(mentorId);
        return ResponseEntity.ok(solicitacaoService.filaDoMentor(mentorId));
    }
}
