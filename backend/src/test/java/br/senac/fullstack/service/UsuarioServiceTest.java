package br.senac.fullstack.service;

import br.senac.fullstack.dto.UsuarioRequestDTO;
import br.senac.fullstack.dto.UsuarioResponseDTO;
import br.senac.fullstack.dto.UsuarioUpdateDTO;
import br.senac.fullstack.model.Usuario;
import br.senac.fullstack.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
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

    private static final String EMAIL_PADRAO = "e@e.com";
    private static final String NOVO_EMAIL = "novo@test.com";
    private static final String NOME_NOVO = "Novo";

    private PasswordEncoder encoder;
    private UsuarioService serviceNullRepo;

    @BeforeEach
    void setUp() {
        encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("hash");
        serviceNullRepo = new UsuarioService(new NullObjectProvider<>(), encoder);
    }

    @Test
    void listarSemRepoRetornaVazio() {
        assertTrue(serviceNullRepo.listar().isEmpty());
    }

    @Test
    void buscarSemRepoLanca503() {
        assertThrows(ResponseStatusException.class, () -> serviceNullRepo.buscarPorId(1L));
    }

    @Test
    void salvarSemRepoLanca503() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("n", EMAIL_PADRAO, "s");
        assertThrows(ResponseStatusException.class, () -> serviceNullRepo.salvar(dto));
    }

    @Test
    void atualizarSemRepoLanca503() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("n", EMAIL_PADRAO, "s");
        assertThrows(ResponseStatusException.class, () -> serviceNullRepo.atualizar(1L, dto));
    }

    @Test
    void definirAtivoSemRepoLanca503() {
        assertThrows(ResponseStatusException.class, () -> serviceNullRepo.definirAtivo(1L, true));
    }

    @Test
    void deletarSemRepoLanca503() {
        assertThrows(ResponseStatusException.class, () -> serviceNullRepo.deletar(1L));
    }

    @Test
    void salvarComRepoDuplicadoLancaConflict() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        when(repo.existsByEmail(EMAIL_PADRAO)).thenReturn(true);
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder);
        UsuarioRequestDTO dto = new UsuarioRequestDTO("n", EMAIL_PADRAO, "s");
        assertThrows(ResponseStatusException.class, () -> s.salvar(dto));
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

        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder);
        UsuarioResponseDTO resp = s.salvar(new UsuarioRequestDTO(NOME_NOVO, "novo@e.com", "senha123"));

        assertNotNull(resp);
        assertEquals(10L, resp.id());
        assertEquals(NOME_NOVO, resp.nome());
        assertEquals("novo@e.com", resp.email());
    }

    @Test
    void buscarComRepoExistenteRetornaUsuario() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("n");
        u.setEmail(EMAIL_PADRAO);
        when(repo.findById(1L)).thenReturn(Optional.of(u));

        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder);
        UsuarioResponseDTO r = s.buscarPorId(1L);
        assertNotNull(r);
        assertEquals("n", r.nome());
    }

    @Test
    void buscarComRepoInexistenteLanca404() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        when(repo.findById(99L)).thenReturn(Optional.empty());
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder);
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

        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder);
        List<UsuarioResponseDTO> lista = s.listar();
        assertEquals(1, lista.size());
        assertEquals("User 1", lista.get(0).nome());
    }

    @Test
    void atualizarComConflitoLanca409() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.existsByEmail("duplicado@test.com")).thenReturn(true);

        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder);
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO(NOME_NOVO, "duplicado@test.com", "pass");

        assertThrows(ResponseStatusException.class, () -> s.atualizar(1L, dto));
    }

    @Test
    void atualizarComSucesso() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Antigo");
        u.setEmail("antigo@test.com");

        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repo.existsByEmail(NOVO_EMAIL)).thenReturn(false);

        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder);

        UsuarioResponseDTO atualizado = s.atualizar(
                1L, new UsuarioUpdateDTO("Novo Nome", NOVO_EMAIL, "novaSenha"));
        assertEquals("Novo Nome", atualizado.nome());
        assertEquals(NOVO_EMAIL, atualizado.email());
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

        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder);

        UsuarioResponseDTO inativo = s.definirAtivo(1L, false);
        assertNotNull(inativo);

        s.deletar(1L);
        verify(repo).deleteById(1L);
    }

    @Test
    void deletarInexistenteLanca404() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        when(repo.existsById(2L)).thenReturn(false);
        UsuarioService s = new UsuarioService(new FixedObjectProvider<>(repo), encoder);
        assertThrows(ResponseStatusException.class, () -> s.deletar(2L));
    }
}
