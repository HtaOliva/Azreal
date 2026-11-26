package br.senac.fullstack.controller;

import br.senac.fullstack.dto.RegistroOficialPocResponseDTO;
import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.StartupUpdateDTO;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.service.RegistroOficialPocService;
import br.senac.fullstack.service.StartupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/startups")
public class StartupController {

    private final StartupService startupService;
    private final ObjectProvider<RegistroOficialPocService> registroServiceProvider;
    private final ObjectProvider<UserDetailsManager> userDetailsManagerProvider;
    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    @Autowired
    public StartupController(
            StartupService startupService,
            ObjectProvider<RegistroOficialPocService> registroServiceProvider,
            ObjectProvider<UserDetailsManager> userDetailsManagerProvider,
            ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
        this.startupService = startupService;
        this.registroServiceProvider = registroServiceProvider;
        this.userDetailsManagerProvider = userDetailsManagerProvider;
        this.passwordEncoderProvider = passwordEncoderProvider;
    }

    public StartupController(
            StartupService startupService,
            ObjectProvider<RegistroOficialPocService> registroServiceProvider) {
        this(startupService, registroServiceProvider, null, null);
    }

    public StartupController(StartupService startupService) {
        this(startupService, null, null, null);
    }

    @PostMapping
    public ResponseEntity<StartupResponseDTO> criar(@Valid @RequestBody StartupRequestDTO request) {
        StartupResponseDTO created = startupService.criar(request);
        registrarCredenciaisLocais(request.email(), request.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private void registrarCredenciaisLocais(String email, String senha) {
        if (userDetailsManagerProvider == null || passwordEncoderProvider == null
                || email == null || senha == null) {
            return;
        }
        UserDetailsManager manager = userDetailsManagerProvider.getIfAvailable();
        PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();
        if (manager != null && encoder != null && !manager.userExists(email)) {
            manager.createUser(User.withUsername(email)
                    .password(encoder.encode(senha))
                    .roles("STARTUP")
                    .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StartupResponseDTO> buscar(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(startupService.buscar(id));
    }

    @GetMapping
    public ResponseEntity<List<StartupResponseDTO>> listar() {
        return ResponseEntity.ok(startupService.listar());
    }

    @GetMapping("/paginado")
    public ResponseEntity<Page<StartupResponseDTO>> listarPaginado(
            @RequestParam(name = "estagio", required = false) Estagio estagio,
            @RequestParam(name = "termoBusca", required = false) String termoBusca,
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(startupService.listarPaginado(estagio, termoBusca, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<StartupResponseDTO> atualizar(@PathVariable("id") UUID id,
                                                        @Valid @RequestBody StartupUpdateDTO request,
                                                        Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            StartupResponseDTO atual = startupService.buscar(id);
            if (atual.email() == null || !atual.email().equals(authentication.getName())) {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.ok(startupService.atualizar(id, request));
    }

    @GetMapping("/{id}/pocs")
    public ResponseEntity<List<RegistroOficialPocResponseDTO>> listarPocsPortfolio(
            @PathVariable("id") UUID id) {
        RegistroOficialPocService service = registroServiceProvider != null
                ? registroServiceProvider.getIfAvailable() : null;
        if (service == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(service.listarPorStartup(id));
    }

    @GetMapping("/{id}/registros-poc")
    public ResponseEntity<List<RegistroOficialPocResponseDTO>> listarRegistrosPocAlias(
            @PathVariable("id") UUID id) {
        return listarPocsPortfolio(id);
    }
}
