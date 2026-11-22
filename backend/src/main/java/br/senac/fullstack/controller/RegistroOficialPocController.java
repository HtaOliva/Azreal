package br.senac.fullstack.controller;

import br.senac.fullstack.dto.RegistroOficialPocResponseDTO;
import br.senac.fullstack.service.RegistroOficialPocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping
public class RegistroOficialPocController {

    private final RegistroOficialPocService registroOficialPocService;

    @Autowired
    public RegistroOficialPocController(RegistroOficialPocService registroOficialPocService) {
        this.registroOficialPocService = registroOficialPocService;
    }

    @GetMapping("/registros-poc/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RegistroOficialPocResponseDTO> buscarPorId(@PathVariable("id") UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(registroOficialPocService.buscarPorId(id));
    }

    @GetMapping("/registros-poc/verificar/{codigoVerificador}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RegistroOficialPocResponseDTO> verificarPorCodigo(
            @PathVariable("codigoVerificador") String codigoVerificador) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(registroOficialPocService.buscarPorCodigoVerificador(codigoVerificador));
    }

    @GetMapping("/pocs/{codigoOuId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RegistroOficialPocResponseDTO> buscarPocPublica(
            @PathVariable("codigoOuId") String codigoOuId) {
        try {
            UUID id = UUID.fromString(codigoOuId);
            return ResponseEntity.status(HttpStatus.OK).body(registroOficialPocService.buscarPorId(id));
        } catch (IllegalArgumentException notUuid) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(registroOficialPocService.buscarPorCodigoVerificador(codigoOuId));
        }
    }
}
