package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.service.AmbienteValidacaoService;
import br.senac.fullstack.service.StartupService;
import br.senac.fullstack.service.TermoPilotoService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private final ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider;
    private final ObjectProvider<StartupService> startupServiceProvider;

    @Autowired
    public TermoPilotoController(
            TermoPilotoService termoPilotoService,
            ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider,
            ObjectProvider<StartupService> startupServiceProvider) {
        this.termoPilotoService = termoPilotoService;
        this.ambienteServiceProvider = ambienteServiceProvider;
        this.startupServiceProvider = startupServiceProvider;
    }

    public TermoPilotoController(TermoPilotoService termoPilotoService) {
        this(termoPilotoService, null, null);
    }

    @GetMapping("/{propostaId}")
    @PreAuthorize("hasRole('STARTUP') or hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<TermoPilotoResponseDTO> buscarPorProposta(
            @PathVariable("propostaId") UUID propostaId,
            Authentication authentication) {
        TermoPiloto termo = termoPilotoService.buscarPorPropostaId(propostaId);
        if (isAcessoTermoNaoAutorizado(termo, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AmbienteValidacaoResponseDTO ambienteRevelado = buscarAmbienteRevelado(termo.getAmbienteValidacaoId());
        return ResponseEntity.ok(TermoPilotoResponseDTO.fromEntity(termo, StatusProposta.EM_TESTE, ambienteRevelado));
    }

    private boolean isAcessoTermoNaoAutorizado(TermoPiloto termo, Authentication authentication) {
        if (authentication == null || isAdmin(authentication) || termo == null) {
            return false;
        }
        if (isStartupDonaDoTermo(termo.getStartupId(), authentication)) {
            return false;
        }
        return !isAmbienteDonoDoTermo(termo.getAmbienteValidacaoId(), authentication);
    }

    private boolean isStartupDonaDoTermo(UUID startupId, Authentication authentication) {
        StartupService startupService = startupServiceProvider != null
                ? startupServiceProvider.getIfAvailable() : null;
        if (startupService == null || startupId == null) {
            return false;
        }
        try {
            StartupResponseDTO startup = startupService.buscarPorEmail(authentication.getName());
            return startup != null && startupId.equals(startup.id());
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isAmbienteDonoDoTermo(UUID ambienteId, Authentication authentication) {
        AmbienteValidacaoService ambienteService = ambienteServiceProvider != null
                ? ambienteServiceProvider.getIfAvailable() : null;
        if (ambienteService == null || ambienteId == null) {
            return false;
        }
        try {
            AmbienteValidacaoResponseDTO ambiente = ambienteService.buscarPorEmail(authentication.getName());
            return ambiente != null && ambienteId.equals(ambiente.id());
        } catch (Exception ignored) {
            return false;
        }
    }

    private AmbienteValidacaoResponseDTO buscarAmbienteRevelado(UUID ambienteId) {
        if (ambienteId == null) {
            return null;
        }
        AmbienteValidacaoService ambienteService = ambienteServiceProvider != null
                ? ambienteServiceProvider.getIfAvailable() : null;
        if (ambienteService == null) {
            return null;
        }
        try {
            return ambienteService.buscar(ambienteId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
