package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AtivarContaRequestDTO;
import br.senac.fullstack.dto.UsuarioRequestDTO;
import br.senac.fullstack.dto.UsuarioResponseDTO;
import br.senac.fullstack.dto.UsuarioUpdateDTO;
import br.senac.fullstack.model.Usuario;
import br.senac.fullstack.repository.UsuarioRepository;
import br.senac.fullstack.service.FixedObjectProvider;
import br.senac.fullstack.service.NullObjectProvider;
import br.senac.fullstack.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CadastroUsuarioControllerTest {

    private CadastroUsuarioController criarComRepositorioMockado() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario usuario = new Usuario();
        usuario.setNome("Maria");
        usuario.setEmail("maria@senac.br");

        when(repo.findAll()).thenReturn(List.of(usuario));
        when(repo.findById(1L)).thenReturn(Optional.of(usuario));
        when(repo.existsByEmail(any())).thenReturn(false);
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.save(any(Usuario.class))).thenReturn(usuario);

        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("hash");

        UsuarioService service = new UsuarioService(new FixedObjectProvider<>(repo), encoder);
        return new CadastroUsuarioController(service);
    }

    @Test
    void listarRetornaOk() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UsuarioService service = new UsuarioService(new NullObjectProvider<>(), encoder);
        CadastroUsuarioController ctrl = new CadastroUsuarioController(service);
        var resp = ctrl.listar();
        assertNotNull(resp);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void buscarPorIdRetornaOk() {
        CadastroUsuarioController ctrl = criarComRepositorioMockado();
        ResponseEntity<UsuarioResponseDTO> resp = ctrl.buscarPorId(1L);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Maria", resp.getBody().nome());
    }

    @Test
    void criarRetornaCreated() {
        CadastroUsuarioController ctrl = criarComRepositorioMockado();
        UsuarioRequestDTO req = new UsuarioRequestDTO("Maria", "maria@senac.br", "senha123");
        ResponseEntity<UsuarioResponseDTO> resp = ctrl.criar(req);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void atualizarRetornaOk() {
        CadastroUsuarioController ctrl = criarComRepositorioMockado();
        UsuarioUpdateDTO req = new UsuarioUpdateDTO("Maria Silva", "maria.silva@senac.br", "novaSenha123");
        ResponseEntity<UsuarioResponseDTO> resp = ctrl.atualizar(1L, req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void definirAtivoRetornaOk() {
        CadastroUsuarioController ctrl = criarComRepositorioMockado();
        AtivarContaRequestDTO req = new AtivarContaRequestDTO(false);
        ResponseEntity<UsuarioResponseDTO> resp = ctrl.definirAtivo(1L, req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void deletarRetornaNoContent() {
        CadastroUsuarioController ctrl = criarComRepositorioMockado();
        ResponseEntity<Void> resp = ctrl.deletar(1L);
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }
}
