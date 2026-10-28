package br.senac.fullstack.controller;

import br.senac.fullstack.dto.CertificadoResponseDTO;
import br.senac.fullstack.service.CertificadoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/startups")
public class CertificadoController {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf", "image/png", "image/jpeg");

    private final CertificadoService certificadoService;

    public CertificadoController(CertificadoService certificadoService) {
        this.certificadoService = certificadoService;
    }

    @PostMapping("/{id}/certificados")
    @PreAuthorize("hasRole('STARTUP') or hasRole('ADMIN')")
    public ResponseEntity<CertificadoResponseDTO> upload(@PathVariable("id") UUID id,
                                                        @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.status(413).build();
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.status(415).build();
        }
        CertificadoResponseDTO resp = certificadoService.registrar(
                id, file.getOriginalFilename(), contentType, file.getSize(), file.getOriginalFilename());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}/certificados")
    @PreAuthorize("hasRole('STARTUP') or hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<CertificadoResponseDTO>> listar(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(certificadoService.listarPorStartup(id));
    }
}
