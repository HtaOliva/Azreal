package br.senac.fullstack;

import br.senac.fullstack.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class FullstackApplicationTests {

    @Test
    void contextLoads() {
        FullstackApplication app = new FullstackApplication();
        assertNotNull(app);
    }

    @Test
    void testarConexaoSucessoEErro() throws Exception {
        FullstackApplication app = new FullstackApplication();
        UsuarioRepository repo = mock(UsuarioRepository.class);
        when(repo.count()).thenReturn(5L);
        app.testarConexao(repo).run();

        when(repo.count()).thenThrow(new RuntimeException("DB down"));
        app.testarConexao(repo).run();
    }
}
