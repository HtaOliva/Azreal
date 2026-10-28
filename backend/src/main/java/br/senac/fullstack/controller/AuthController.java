package br.senac.fullstack.controller;

import br.senac.fullstack.dto.LoginRequestDTO;
import br.senac.fullstack.dto.LoginResponseDTO;
import br.senac.fullstack.dto.MeResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        List<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).toList();
        return ResponseEntity.ok(new LoginResponseDTO("mock-jwt-token", 7200, roles));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        List<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).toList();
        String perfil = roles.contains("ROLE_ADMIN") ? "ADMIN"
                : roles.contains("ROLE_MENTOR") ? "MENTOR"
                : roles.contains("ROLE_STARTUP") ? "STARTUP" : "NONE";
        return ResponseEntity.ok(new MeResponseDTO(
                UUID.randomUUID(), authentication.getName(), authentication.getName(), roles, perfil));
    }
}
