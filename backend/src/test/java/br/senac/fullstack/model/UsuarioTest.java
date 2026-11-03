package br.senac.fullstack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    void testGettersSettersEConstrutores() {
        Usuario u1 = new Usuario();
        u1.setId(1L);
        u1.setNome("Admin");
        u1.setEmail("admin@senac.br");
        u1.setSenhaHash("hash");
        u1.setAtivo(true);

        assertEquals(1L, u1.getId());
        assertEquals("Admin", u1.getNome());
        assertEquals("admin@senac.br", u1.getEmail());
        assertEquals("hash", u1.getSenhaHash());
        assertTrue(u1.isAtivo());

        Usuario u2 = new Usuario("Dev", "dev@senac.br", "hash2");
        assertEquals("Dev", u2.getNome());
        assertEquals("dev@senac.br", u2.getEmail());
    }
}
