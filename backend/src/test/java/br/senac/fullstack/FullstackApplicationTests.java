package br.senac.fullstack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FullstackApplicationTests {

    @Test
    void contextLoads() {
        FullstackApplication app = new FullstackApplication();
        assertNotNull(app);
    }
}
