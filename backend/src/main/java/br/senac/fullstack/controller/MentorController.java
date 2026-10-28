package br.senac.fullstack.controller;

import br.senac.fullstack.dto.MentorRequestDTO;
import br.senac.fullstack.dto.MentorResponseDTO;
import br.senac.fullstack.dto.MentorUpdateDTO;
import br.senac.fullstack.service.MentorService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/mentors")
public class MentorController {

    private final MentorService mentorService;

    public MentorController(MentorService mentorService) {
        this.mentorService = mentorService;
    }

    @PostMapping
    public ResponseEntity<MentorResponseDTO> criar(@Valid @RequestBody MentorRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mentorService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorResponseDTO> buscar(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(mentorService.buscar(id));
    }

    @GetMapping
    public ResponseEntity<List<MentorResponseDTO>> listar() {
        return ResponseEntity.ok(mentorService.listar());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<MentorResponseDTO> atualizar(@PathVariable("id") UUID id,
                                                       @Valid @RequestBody MentorUpdateDTO request,
                                                       Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            MentorResponseDTO atual = mentorService.buscar(id);
            if (atual.email() == null || !atual.email().equals(authentication.getName())) {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.ok(mentorService.atualizar(id, request));
    }
}
