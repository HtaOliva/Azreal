package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoResponseDTO;
import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.dto.RecusaPropostaRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.service.AmbienteValidacaoService;
import br.senac.fullstack.service.DesafioValidacaoService;
import br.senac.fullstack.service.PropostaPocService;
import br.senac.fullstack.service.StartupService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/propostas-poc")
public class PropostaPocController {

    private final PropostaPocService propostaPocService;
    private final ObjectProvider<StartupService> startupServiceProvider;
    private final ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider;
    private final ObjectProvider<DesafioValidacaoService> desafioServiceProvider;

    @Autowired
    public PropostaPocController(
            PropostaPocService propostaPocService,
            ObjectProvider<StartupService> startupServiceProvider,
            ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider,
            ObjectProvider<DesafioValidacaoService> desafioServiceProvider) {
        this.propostaPocService = propostaPocService;
        this.startupServiceProvider = startupServiceProvider;
        this.ambienteServiceProvider = ambienteServiceProvider;
        this.desafioServiceProvider = desafioServiceProvider;
    }

    public PropostaPocController(
            PropostaPocService propostaPocService,
            ObjectProvider<StartupService> startupServiceProvider) {
        this(propostaPocService, startupServiceProvider, null, null);
    }

    @PostMapping
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<PropostaPocResponseDTO> criar(
            @Valid @RequestBody PropostaPocRequestDTO request,
            Authentication authentication) {
        if (authentication != null && !isAdmin(authentication)) {
            StartupService startupService = startupServiceProvider.getIfAvailable();
            if (startupService != null) {
                try {
                    StartupResponseDTO startup = startupService.buscarPorEmail(authentication.getName());
                    if (startup != null && request.startupId() != null
                            && !request.startupId().equals(startup.id())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                } catch (Exception ignored) {
                    // Prossegue com validacao no service caso nao encontre
                }
            }
        }
        PropostaPocResponseDTO criada = propostaPocService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<PropostaPocResponseDTO> cancelar(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        if (authentication != null && !isAdmin(authentication)) {
            StartupService startupService = startupServiceProvider.getIfAvailable();
            if (startupService != null) {
                try {
                    StartupResponseDTO startup = startupService.buscarPorEmail(authentication.getName());
                    PropostaPocResponseDTO proposta = propostaPocService.buscar(id);
                    if (startup != null && !proposta.startupId().equals(startup.id())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                } catch (Exception ignored) {
                    // Prossegue para o service
                }
            }
        }
        return ResponseEntity.ok(propostaPocService.cancelar(id));
    }

    @PostMapping("/{id}/recusar")
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<PropostaPocResponseDTO> recusar(
            @PathVariable("id") UUID id,
            @Valid @RequestBody RecusaPropostaRequestDTO request,
            Authentication authentication) {
        if (isAcessoRecusaNaoAutorizado(id, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(propostaPocService.recusar(id, request.motivoRecusa()));
    }

    private boolean isAcessoRecusaNaoAutorizado(UUID id, Authentication authentication) {
        if (authentication == null || isAdmin(authentication)) {
            return false;
        }
        AmbienteValidacaoService ambienteService = ambienteServiceProvider != null
                ? ambienteServiceProvider.getIfAvailable() : null;
        DesafioValidacaoService desafioService = desafioServiceProvider != null
                ? desafioServiceProvider.getIfAvailable() : null;

        if (ambienteService == null || desafioService == null) {
            return false;
        }

        try {
            AmbienteValidacaoResponseDTO ambiente = ambienteService.buscarPorEmail(authentication.getName());
            PropostaPocResponseDTO proposta = propostaPocService.buscar(id);
            DesafioValidacaoResponseDTO desafio = desafioService.buscar(proposta.desafioValidacaoId());
            return ambiente != null && desafio != null && !desafio.ambienteValidacaoId().equals(ambiente.id());
        } catch (Exception ignored) {
            return false;
        }
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<List<PropostaPocResponseDTO>> listarMinhas(
            @RequestParam(name = "startupId", required = false) UUID startupIdParam,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID targetStartupId = startupIdParam;
        StartupService startupService = startupServiceProvider.getIfAvailable();

        if (targetStartupId == null && startupService != null) {
            try {
                StartupResponseDTO startup = startupService.buscarPorEmail(authentication.getName());
                if (startup != null) {
                    targetStartupId = startup.id();
                }
            } catch (Exception ignored) {
                // Continua nulo
            }
        }

        if (targetStartupId == null) {
            return ResponseEntity.ok(List.of());
        }

        if (!isAdmin(authentication) && startupIdParam != null && startupService != null) {
            try {
                StartupResponseDTO startupAutenticada = startupService.buscarPorEmail(authentication.getName());
                if (startupAutenticada != null && !targetStartupId.equals(startupAutenticada.id())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } catch (Exception ignored) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(propostaPocService.listarPorStartup(targetStartupId));
    }

    @GetMapping("/fila")
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<List<PropostaPocResponseDTO>> consultarFila(
            @RequestParam(name = "ambienteId", required = false) UUID ambienteIdParam,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (isAcessoFilaNaoAutorizado(ambienteIdParam, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UUID targetAmbienteId = resolverAmbienteIdFila(ambienteIdParam, authentication);
        if (targetAmbienteId == null) {
            return ResponseEntity.ok(List.of());
        }

        Set<UUID> desafioIds = obterDesafioIdsPorAmbiente(targetAmbienteId);
        if (desafioIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(propostaPocService.listarFilaAmbiente(desafioIds));
    }

    private boolean isAcessoFilaNaoAutorizado(UUID ambienteIdParam, Authentication authentication) {
        if (isAdmin(authentication)) {
            return false;
        }
        AmbienteValidacaoService ambienteService = ambienteServiceProvider != null
                ? ambienteServiceProvider.getIfAvailable() : null;
        if (ambienteService == null) {
            return false;
        }
        try {
            AmbienteValidacaoResponseDTO ambiente = ambienteService.buscarPorEmail(authentication.getName());
            return ambiente != null && ambienteIdParam != null && !ambienteIdParam.equals(ambiente.id());
        } catch (Exception ignored) {
            return true;
        }
    }

    private UUID resolverAmbienteIdFila(UUID ambienteIdParam, Authentication authentication) {
        if (isAdmin(authentication)) {
            return ambienteIdParam;
        }
        AmbienteValidacaoService ambienteService = ambienteServiceProvider != null
                ? ambienteServiceProvider.getIfAvailable() : null;
        if (ambienteService == null) {
            return ambienteIdParam;
        }
        try {
            AmbienteValidacaoResponseDTO ambiente = ambienteService.buscarPorEmail(authentication.getName());
            return ambiente != null ? ambiente.id() : ambienteIdParam;
        } catch (Exception ignored) {
            return null;
        }
    }

    private Set<UUID> obterDesafioIdsPorAmbiente(UUID targetAmbienteId) {
        DesafioValidacaoService desafioService = desafioServiceProvider != null
                ? desafioServiceProvider.getIfAvailable() : null;
        if (desafioService == null) {
            return Set.of();
        }
        List<DesafioValidacaoResponseDTO> desafios = desafioService.listarPorAmbiente(targetAmbienteId);
        if (desafios.isEmpty()) {
            return Set.of();
        }
        return desafios.stream()
                .map(DesafioValidacaoResponseDTO::id)
                .collect(Collectors.toSet());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropostaPocResponseDTO> buscar(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(propostaPocService.buscar(id));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
