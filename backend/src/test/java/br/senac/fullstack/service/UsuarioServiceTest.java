package br.senac.fullstack.service;

import br.senac.fullstack.dto.UsuarioRequestDTO;
import br.senac.fullstack.dto.UsuarioResponseDTO;
import br.senac.fullstack.dto.UsuarioUpdateDTO;
import br.senac.fullstack.model.Usuario;
import br.senac.fullstack.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    void salvarComSucesso() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        when(repo.existsByEmail("novo@e.com")).thenReturn(false);
        when(repo.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        UsuarioResponseDTO resp = s.salvar(new UsuarioRequestDTO("Novo", "novo@e.com", "senha123"));

        assertNotNull(resp);
        assertEquals(10L, resp.id());
        assertEquals("Novo", resp.nome());
        assertEquals("novo@e.com", resp.email());
    }

    @Test
    void buscarComRepoExistenteRetornaUsuario() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("n");
        u.setEmail("e@e.com");
        when(repo.findById(1L)).thenReturn(Optional.of(u));
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        UsuarioResponseDTO r = s.buscarPorId(1L);
        assertNotNull(r);
        assertEquals("n", r.nome());
    }

    @Test
    void buscarComRepoInexistenteLanca404() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        when(repo.findById(99L)).thenReturn(Optional.empty());
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        assertThrows(ResponseStatusException.class, () -> s.buscarPorId(99L));
    }

    @Test
    void listarComRepoRetornaUsuarios() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("User 1");
        u.setEmail("u1@test.com");
        when(repo.findAll()).thenReturn(List.of(u));

        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());
        List<UsuarioResponseDTO> lista = s.listar();
        assertEquals(1, lista.size());
        assertEquals("User 1", lista.get(0).nome());
    }

    @Test
    void atualizarComSucessoEConflito() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Antigo");
        u.setEmail("antigo@test.com");

        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repo.existsByEmail("duplicado@test.com")).thenReturn(true);
        when(repo.existsByEmail("novo@test.com")).thenReturn(false);

        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());

        assertThrows(ResponseStatusException.class,
                () -> s.atualizar(1L, new UsuarioUpdateDTO("Novo", "duplicado@test.com", "pass")));

        UsuarioResponseDTO atualizado = s.atualizar(
                1L, new UsuarioUpdateDTO("Novo Nome", "novo@test.com", "novaSenha"));
        assertEquals("Novo Nome", atualizado.nome());
        assertEquals("novo@test.com", atualizado.email());
    }

    @Test
    void definirAtivoEDeletarComRepo() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setId(1L);
        u.setAtivo(true);
        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.existsById(2L)).thenReturn(false);

        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder());

        UsuarioResponseDTO inativo = s.definirAtivo(1L, false);
        assertNotNull(inativo);

        s.deletar(1L);
        verify(repo).deleteById(1L);

        assertThrows(ResponseStatusException.class, () -> s.deletar(2L));
    }
}
