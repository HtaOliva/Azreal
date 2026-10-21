package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.AceiteTermoResponseDTO;
import br.senac.fullstack.service.AceiteTermoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/termos")
public class AceiteTermoController {

    private final AceiteTermoService aceiteTermoService;

    public AceiteTermoController(AceiteTermoService aceiteTermoService) {
        this.aceiteTermoService = aceiteTermoService;
    }

    @PostMapping("/aceite")
    public ResponseEntity<AceiteTermoResponseDTO> registrar(@Valid @RequestBody AceiteTermoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aceiteTermoService.registrar(request));
    }
}
