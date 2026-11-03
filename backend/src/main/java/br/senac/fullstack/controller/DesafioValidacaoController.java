package br.senac.fullstack.controller;

import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoRequestDTO;
import br.senac.fullstack.dto.DesafioValidacaoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoUpdateDTO;
import br.senac.fullstack.service.DesafioValidacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/desafios")
public class DesafioValidacaoController {

    private final DesafioValidacaoService desafioValidacaoService;

    public DesafioValidacaoController(DesafioValidacaoService desafioValidacaoService) {
        this.desafioValidacaoService = desafioValidacaoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<DesafioValidacaoResponseDTO> criar(
            @Valid @RequestBody DesafioValidacaoRequestDTO request) {
        DesafioValidacaoResponseDTO criado = desafioValidacaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<DesafioAnonimoResponseDTO>> listarAtivosAnonimos() {
        return ResponseEntity.ok(desafioValidacaoService.listarAtivosAnonimos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<DesafioValidacaoResponseDTO> buscar(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        DesafioValidacaoResponseDTO atual = desafioValidacaoService.buscar(id);
        if (!temPermissaoDeAcesso(atual, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(atual);
    }

    @GetMapping("/ambiente/{ambienteId}")
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<List<DesafioValidacaoResponseDTO>> listarPorAmbiente(
            @PathVariable("ambienteId") UUID ambienteId,
            Authentication authentication) {
        List<DesafioValidacaoResponseDTO> lista = desafioValidacaoService.listarPorAmbiente(ambienteId);
        if (!lista.isEmpty() && !temPermissaoDeAcesso(lista.get(0), authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<DesafioValidacaoResponseDTO> atualizar(
            @PathVariable("id") UUID id,
            @Valid @RequestBody DesafioValidacaoUpdateDTO request,
            Authentication authentication) {
        DesafioValidacaoResponseDTO atual = desafioValidacaoService.buscar(id);
        if (!temPermissaoDeAcesso(atual, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(desafioValidacaoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<DesafioValidacaoResponseDTO> inativar(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        DesafioValidacaoResponseDTO atual = desafioValidacaoService.buscar(id);
        if (!temPermissaoDeAcesso(atual, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(desafioValidacaoService.inativar(id));
    }

    private boolean temPermissaoDeAcesso(DesafioValidacaoResponseDTO atual, Authentication authentication) {
        if (authentication == null || atual == null) {
            return false;
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return true;
        }
        boolean isAmbiente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AMBIENTE_VALIDACAO"));
        if (isAmbiente) {
            if (atual.emailContato() == null || atual.emailContato().isBlank()) {
                return true;
            }
            return atual.emailContato().equalsIgnoreCase(authentication.getName());
        }
        return false;
    }
}
