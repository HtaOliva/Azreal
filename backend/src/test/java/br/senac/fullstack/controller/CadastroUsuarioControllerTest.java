package br.senac.fullstack.controller;

import br.senac.fullstack.service.UsuarioService;
import br.senac.fullstack.service.NullObjectProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class CadastroUsuarioControllerTest {

    @Test
    void listarRetornaOk() {
        PasswordEncoder encoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        UsuarioService service = new UsuarioService(new NullObjectProvider<>(), encoder);
        CadastroUsuarioController ctrl = new CadastroUsuarioController(service);
        var resp = ctrl.listar();
        assertNotNull(resp);
    }
}
