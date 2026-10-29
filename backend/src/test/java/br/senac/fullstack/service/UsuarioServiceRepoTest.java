package br.senac.fullstack.service;

import br.senac.fullstack.dto.UsuarioRequestDTO;
import br.senac.fullstack.dto.UsuarioResponseDTO;
import br.senac.fullstack.dto.UsuarioUpdateDTO;
import br.senac.fullstack.model.Usuario;
import br.senac.fullstack.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioServiceRepoTest {

    private PasswordEncoder encoder() {
        PasswordEncoder e = mock(PasswordEncoder.class);
        when(e.encode(any())).thenReturn("hash");
        return e;
    }

    @Test
    void salvarComRepo() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setNome("n");
        u.setEmail("e@e.com");
        when(repo.existsByEmail("e@e.com")).thenReturn(false);
        when(repo.save(any(Usuario.class))).thenReturn(u);
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        UsuarioResponseDTO r = s.salvar(new UsuarioRequestDTO("n", "e@e.com", "s"));
        assertNotNull(r);
    }

    @Test
    void buscarComRepoExistente() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setNome("n");
        u.setEmail("e@e.com");
        when(repo.findById(1L)).thenReturn(Optional.of(u));
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        UsuarioResponseDTO r = s.buscarPorId(1L);
        assertNotNull(r);
        assertEquals("n", r.nome());
    }

    @Test
    void listarComRepo() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setNome("n");
        when(repo.findAll()).thenReturn(List.of(u));
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        assertFalse(s.listar().isEmpty());
    }

    @Test
    void atualizarComRepo() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setNome("n");
        u.setEmail("e@e.com");
        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.existsByEmail("new@e.com")).thenReturn(false);
        when(repo.save(any(Usuario.class))).thenReturn(u);
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        UsuarioResponseDTO r = s.atualizar(1L, new br.senac.fullstack.dto.UsuarioUpdateDTO("n2", "new@e.com", "s"));
        assertNotNull(r);
    }

    @Test
    void definirAtivoComRepo() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setNome("n");
        u.setEmail("e@e.com");
        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.save(any(Usuario.class))).thenReturn(u);
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        UsuarioResponseDTO r = s.definirAtivo(1L, false);
        assertNotNull(r);
    }

    @Test
    void deletarComRepo() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        when(repo.existsById(1L)).thenReturn(true);
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        assertDoesNotThrow(() -> s.deletar(1L));
    }
}
