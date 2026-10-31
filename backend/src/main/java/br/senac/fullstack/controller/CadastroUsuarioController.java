package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AtivarContaRequestDTO;
import br.senac.fullstack.dto.UsuarioRequestDTO;
import br.senac.fullstack.dto.UsuarioResponseDTO;
import br.senac.fullstack.dto.UsuarioUpdateDTO;
import br.senac.fullstack.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class CadastroUsuarioController {

    private final UsuarioService usuarioService;

    public CadastroUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO criado = usuarioService.salvar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable("id") Long id,
                                                      @Valid @RequestBody UsuarioUpdateDTO request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @PutMapping("/{id}/ativo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> definirAtivo(@PathVariable("id") Long id,
                                                          @Valid @RequestBody AtivarContaRequestDTO request) {
        return ResponseEntity.ok(usuarioService.definirAtivo(id, request.ativo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
