package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AmbienteValidacaoRequestDTO;
import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.AmbienteValidacaoUpdateDTO;
import br.senac.fullstack.service.AmbienteValidacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ambientes-validacao")
public class AmbienteValidacaoController {

    private final AmbienteValidacaoService ambienteValidacaoService;
    private final ObjectProvider<UserDetailsManager> userDetailsManagerProvider;
    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    @Autowired
    public AmbienteValidacaoController(
            AmbienteValidacaoService ambienteValidacaoService,
            ObjectProvider<UserDetailsManager> userDetailsManagerProvider,
            ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
        this.ambienteValidacaoService = ambienteValidacaoService;
        this.userDetailsManagerProvider = userDetailsManagerProvider;
        this.passwordEncoderProvider = passwordEncoderProvider;
    }

    public AmbienteValidacaoController(AmbienteValidacaoService ambienteValidacaoService) {
        this(ambienteValidacaoService, null, null);
    }

    @PostMapping
    public ResponseEntity<AmbienteValidacaoResponseDTO> criar(
            @Valid @RequestBody AmbienteValidacaoRequestDTO request) {
        AmbienteValidacaoResponseDTO created = ambienteValidacaoService.criar(request);
        registrarCredenciaisLocais(request.emailContato(), request.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<AmbienteValidacaoResponseDTO> meuAmbiente(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(ambienteValidacaoService.buscarPorEmail(authentication.getName()));
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
                    .roles("AMBIENTE_VALIDACAO")
                    .build());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<AmbienteValidacaoResponseDTO> buscar(@PathVariable("id") UUID id,
                                                               Authentication authentication) {
        AmbienteValidacaoResponseDTO atual = ambienteValidacaoService.buscar(id);
        if (!temPermissaoDeAcesso(atual, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(atual);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AmbienteValidacaoResponseDTO>> listar() {
        return ResponseEntity.ok(ambienteValidacaoService.listar());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AMBIENTE_VALIDACAO') or hasRole('ADMIN')")
    public ResponseEntity<AmbienteValidacaoResponseDTO> atualizar(@PathVariable("id") UUID id,
                                                                  @Valid @RequestBody AmbienteValidacaoUpdateDTO request,
                                                                  Authentication authentication) {
        AmbienteValidacaoResponseDTO atual = ambienteValidacaoService.buscar(id);
        if (!temPermissaoDeAcesso(atual, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(ambienteValidacaoService.atualizar(id, request));
    }

    private boolean temPermissaoDeAcesso(AmbienteValidacaoResponseDTO atual, Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        if (atual == null) {
            return false;
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return true;
        }
        return atual.emailContato() != null && atual.emailContato().equals(authentication.getName());
    }
}
