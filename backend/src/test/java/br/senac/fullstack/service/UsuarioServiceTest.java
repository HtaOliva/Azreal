package br.senac.fullstack.service;

import br.senac.fullstack.dto.UsuarioRequestDTO;
import br.senac.fullstack.dto.UsuarioResponseDTO;
import br.senac.fullstack.dto.UsuarioUpdateDTO;
import br.senac.fullstack.model.Usuario;
import br.senac.fullstack.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private PasswordEncoder encoder() {
        PasswordEncoder e = mock(PasswordEncoder.class);
        when(e.encode(any())).thenReturn("hash");
        return e;
    }

    private UsuarioService serviceWithNullRepo() {
        return new UsuarioService(new NullObjectProvider<>(), encoder());
    }

    @Test
    void listarSemRepoRetornaVazio() {
        assertTrue(serviceWithNullRepo().listar().isEmpty());
    }

    @Test
    void buscarSemRepoLanca503() {
        assertThrows(Exception.class, () -> serviceWithNullRepo().buscarPorId(1L));
    }

    @Test
    void salvarSemRepoLanca503() {
        assertThrows(Exception.class, () -> serviceWithNullRepo().salvar(new UsuarioRequestDTO("n", "e@e.com", "s")));
    }

    @Test
    void atualizarSemRepoLanca503() {
        assertThrows(Exception.class, () -> serviceWithNullRepo().atualizar(1L, new UsuarioUpdateDTO("n", "e@e.com", "s")));
    }

    @Test
    void definirAtivoSemRepoLanca503() {
        assertThrows(Exception.class, () -> serviceWithNullRepo().definirAtivo(1L, true));
    }

    @Test
    void deletarSemRepoLanca503() {
        assertThrows(Exception.class, () -> serviceWithNullRepo().deletar(1L));
    }

    @Test
    void salvarComRepoDuplicadoLancaConflict() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        when(repo.existsByEmail("e@e.com")).thenReturn(true);
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        assertThrows(Exception.class, () -> s.salvar(new UsuarioRequestDTO("n", "e@e.com", "s")));
    }

    @Test
    void buscarComRepoExistenteRetornaUsuario() {
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
}
