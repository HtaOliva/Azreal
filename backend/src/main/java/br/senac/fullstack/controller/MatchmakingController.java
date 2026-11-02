package br.senac.fullstack.controller;

import br.senac.fullstack.dto.DesafioRecomendadoDTO;
import br.senac.fullstack.model.Startup;
import br.senac.fullstack.service.MatchmakingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/matchmaking")
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @GetMapping("/desafios-compativeis")
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<List<DesafioRecomendadoDTO>> obterDesafiosCompativeis(
            @RequestParam(name = "startupId", required = false) UUID startupId,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Startup startup;
        if (startupId != null) {
            startup = matchmakingService.obterStartup(startupId);
            if (!isAdmin && (startup.getEmail() == null
                    || !startup.getEmail().equalsIgnoreCase(authentication.getName()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            startup = matchmakingService.obterStartupPorEmail(authentication.getName());
        }

        List<DesafioRecomendadoDTO> resultado = matchmakingService.recomendarDesafiosParaStartup(startup.getId(), null);
        return ResponseEntity.ok(resultado);
    }
}
